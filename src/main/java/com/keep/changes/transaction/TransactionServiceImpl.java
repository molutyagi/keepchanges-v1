package com.keep.changes.transaction;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.HttpStatus;

import com.keep.changes.exception.ResourceNotFoundException;
import com.keep.changes.fundraiser.Fundraiser;
import com.keep.changes.fundraiser.FundraiserRepository;
import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;

import jakarta.transaction.Transactional;

@Service
public class TransactionServiceImpl implements TransactionService {

	@Value("${razorpay.key.id}")
	private String RAZORPAY_KEY_ID;

	@Value("${razorpay.key.secret}")
	private String RAZORPAY_KEY_SECRET;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private FundraiserRepository fundraiserRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Override
	@Transactional
	public String createOrder(OrderRequest or) {

		try {
//			create order
			RazorpayClient razorpay = new RazorpayClient(RAZORPAY_KEY_ID, RAZORPAY_KEY_SECRET);

			JSONObject orderRequest = new JSONObject();
			orderRequest.put("amount", or.getTotalAmount() * 100); // amount in the smallest currency unit
			orderRequest.put("currency", or.getCurrency());
			orderRequest.put("receipt", "fundraiser_" + or.getFundraiserId());

			Order order = razorpay.orders.create(orderRequest);

			JSONObject json = order.toJson();
			json.put("key", RAZORPAY_KEY_ID);

			System.out.println("order: " + order);

			OrderEntity orderEntity = this.modelMapper.map(or, OrderEntity.class);
			orderEntity.setRazorpayOrderId(order.get("id"));

			System.out.println("transaction");
			System.out.println(orderEntity.getRazorpayOrderId());

			System.out.println("heereeeeeeeeeeeee");
			this.orderRepository.save(orderEntity);
			System.out.println("ye bhi chala");

			return order.toString();
		} catch (RazorpayException e) {
//			response.setErrorMessage(e.getMessage());
			System.out.println(e.getLocalizedMessage());
		}

		return null;
	}

	@Override
	@Transactional
	@Async
	public CompletableFuture<PaymentVerificationResult> verifyTransaction(String razorpay_payment_id,
			String razorpay_order_id, String razorpay_signature) {

		OrderEntity orderEntity = this.orderRepository.findByRazorpayOrderId(razorpay_order_id)
				.orElseThrow(() -> new ResourceNotFoundException("Order", "Id", razorpay_order_id));

		JSONObject options = new JSONObject();
		options.put("razorpay_order_id", orderEntity.getRazorpayOrderId());
		options.put("razorpay_payment_id", razorpay_payment_id);
		options.put("razorpay_signature", razorpay_signature);

		System.out.println("in verify impl");

		return CompletableFuture.supplyAsync(() -> {
			try {
				boolean verifyPaymentSignature = Utils.verifyPaymentSignature(options, RAZORPAY_KEY_SECRET);

				if (verifyPaymentSignature && this.verifyPayment(razorpay_payment_id)) {

					System.out.println("fundraiser id : " + orderEntity.getFundraiserId());

					Fundraiser fundraiser = this.fundraiserRepository.findById(orderEntity.getFundraiserId())
							.orElseThrow(() -> new ResourceNotFoundException("Fundraiser", "Id",
									orderEntity.getFundraiserId()));

					Transaction transaction = this.modelMapper.map(orderEntity, Transaction.class);
					transaction.setFundraiser(fundraiser);
					transaction.setRazorpayPaymentId(razorpay_payment_id);
					transaction.setRazorpaySignature(razorpay_signature);
					transaction.setStatus("captured");

					fundraiser.setRaised(fundraiser.getRaised() + transaction.getDonationAmount());
					this.fundraiserRepository.save(fundraiser);
					this.transactionRepository.save(transaction);
					return new PaymentVerificationResult(true, razorpay_payment_id, orderEntity.getTotalAmount(),
							orderEntity.getTipAmount(), orderEntity.getDonationAmount(), "success");
				} else {
					return new PaymentVerificationResult(false, razorpay_payment_id, orderEntity.getTotalAmount(),
							orderEntity.getTipAmount(), orderEntity.getDonationAmount(), "failed");
				}
			} catch (RazorpayException e) {
				e.printStackTrace();
				return new PaymentVerificationResult(false, razorpay_payment_id, orderEntity.getTotalAmount(),
						orderEntity.getTipAmount(), orderEntity.getDonationAmount(), "error");
			}
		});
	}

	private ResponseEntity<Void> redirectToPaymentResult(PaymentVerificationResult result) {
		Map<String, Object> data = new HashMap<>();
		data.put("paymentId", result.getPaymentId());
		data.put("amount", result.getTotalAmount());
		data.put("status", result.getStatus());

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		data.forEach((key, value) -> params.add(key, String.valueOf(value)));

		UriComponentsBuilder builder = UriComponentsBuilder
				.fromUriString("http://localhost:5173/fundraiser/" + result.getStatus()).queryParams(params);
		URI uri = builder.build().toUri();

		HttpHeaders headers = new HttpHeaders();
		headers.setLocation(uri);

		return new ResponseEntity<>(headers, HttpStatus.FOUND);

	}

	private boolean verifyPayment(String paymentId) {
		try {
			RazorpayClient razorpay = new RazorpayClient(RAZORPAY_KEY_ID, RAZORPAY_KEY_SECRET);
			Payment payment = razorpay.payments.fetch(paymentId);

			String paymentStatus = payment.get("status");

			System.out.println("payment status: " + paymentStatus);

			if (paymentStatus.equals("captured")) {
				return true;
			}
		} catch (RazorpayException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public TransactionDto saveTransaction(Long fId, TransactionDto dto) {
		Transaction transaction = this.modelMapper.map(dto, Transaction.class);

		System.out.println();
		Fundraiser fundraiser = this.fundraiserRepository.findById(fId)
				.orElseThrow(() -> new ResourceNotFoundException("Fundraiser", "id", fId));

		transaction.setFundraiser(fundraiser);

		Transaction savedTransaction = this.transactionRepository.save(transaction);
		return this.modelMapper.map(savedTransaction, TransactionDto.class);
	}

	@Override
	public List<Transaction> getAllTransactions() {
		List<Transaction> all = this.transactionRepository.findAll();

		return all;
	}

	private String generateSignature(String data, String secretKey) throws Exception {
		Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
		hmacSHA256.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
		byte[] hash = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));
		return new String(Base64.getEncoder().encode(hash));
	}

}
