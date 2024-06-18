package com.keep.changes.transaction;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.keep.changes.exception.ResourceNotFoundException;
import com.keep.changes.fundraiser.Fundraiser;
import com.keep.changes.fundraiser.FundraiserRepository;
import com.razorpay.Order;
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
	private TransactionRepository transactionRepository;

	@Autowired
	private FundraiserRepository fundraiserRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Override
	@Transactional
	public String createOrder(TransactionRequest tr) {

		TransactionResponse response = new TransactionResponse();

		Transaction transaction = new Transaction();

		try {
			RazorpayClient razorpay = new RazorpayClient(RAZORPAY_KEY_ID, RAZORPAY_KEY_SECRET);

			JSONObject orderRequest = new JSONObject();
			orderRequest.put("amount", tr.getTotalAmount() * 100); // amount in the smallest currency unit
			orderRequest.put("currency", tr.getCurrency());
			orderRequest.put("receipt", "fundraiser_" + tr.getFundraiserId());

			Order order = razorpay.orders.create(orderRequest);

			JSONObject json = order.toJson();
			json.put("key", RAZORPAY_KEY_ID);

			System.out.println(order);

			transaction.setRazorpayOrderId(order.get("id"));
			transaction.setCurrency(order.get("currency"));
			transaction.setTotalAmount(tr.getTotalAmount());

			System.out.println("transaction");
			System.out.println(transaction.getRazorpayOrderId());

			System.out.println("heereeeeeeeeeeeee");
			this.transactionRepository.save(transaction);
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
	public CompletableFuture<Boolean> verifyTransaction(String razorpay_payment_id, String razorpay_order_id,
			String razorpay_signature) {
		Transaction transaction = this.transactionRepository.findByRazorpayOrderId(razorpay_order_id)
				.orElseThrow(() -> new ResourceNotFoundException("Transaction", "Order Id", razorpay_order_id));

		JSONObject options = new JSONObject();
		options.put("razorpay_order_id", transaction.getRazorpayOrderId());
		options.put("razorpay_payment_id", razorpay_payment_id);
		options.put("razorpay_signature", razorpay_signature);

		return CompletableFuture.supplyAsync(() -> {
			try {
				return Utils.verifyPaymentSignature(options, RAZORPAY_KEY_SECRET);
			} catch (RazorpayException e) {
				e.printStackTrace();
				return false;
			}
		});
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
