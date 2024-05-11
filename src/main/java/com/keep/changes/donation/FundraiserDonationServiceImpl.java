package com.keep.changes.donation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.keep.changes.exception.ResourceNotFoundException;
import com.keep.changes.fundraiser.Fundraiser;
import com.keep.changes.fundraiser.FundraiserRepository;
import com.keep.changes.user.User;
import com.keep.changes.user.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class FundraiserDonationServiceImpl implements FundraiserDonationService {

	@Autowired
	private final FundraiserDonationRepository donationRepository;

	@Autowired
	private final FundraiserRepository fundraiserRepository;

	@Autowired
	private final UserRepository userRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Override
	public FundraiserDonationDto addFundraiserDonation(Long fId, FundraiserDonationDto donationDto) {

//		get currently logged in user
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String loggedInUser = authentication.getName();
		User user = this.userRepository.findByEmail(loggedInUser)
				.orElseThrow(() -> new ResourceNotFoundException("User", "Username", loggedInUser));

//		get fundraiser
		Fundraiser fundraiser = this.fundraiserRepository.findById(fId)
				.orElseThrow(() -> new ResourceNotFoundException("Fundraiser", "Id", fId));

//		convert dto to entity
		FundraiserDonation donation = this.modelMapper.map(donationDto, FundraiserDonation.class);

//		set donor and fundraiser to donation
		donation.setDonor(user);
		donation.setFundraiser(fundraiser);

//		save to repository
		FundraiserDonation savedDonation = this.donationRepository.save(donation);

		return this.modelMapper.map(savedDonation, FundraiserDonationDto.class);
	}

	@Override
	public FundraiserDonationDto putUpdateDonation(Long fDId, FundraiserDonationDto donationDto) {

		FundraiserDonation donation = this.donationRepository.findById(fDId)
				.orElseThrow(() -> new ResourceNotFoundException("Donation", "Id", fDId));

		donation.putUpdateDonation(fDId, donationDto.getDonationAmount(), donationDto.getTransactionId());

		FundraiserDonation updatedDonation = this.donationRepository.save(donation);

		return this.modelMapper.map(updatedDonation, FundraiserDonationDto.class);
	}

	@Override
	public FundraiserDonationDto patchUpdateDonation(Long fDId, FundraiserDonationDto partialDonationDto) {

		FundraiserDonation donation = this.donationRepository.findById(fDId)
				.orElseThrow(() -> new ResourceNotFoundException("Donation", "Id", fDId));

		FundraiserDonation partialDonation = this.modelMapper.map(partialDonationDto, FundraiserDonation.class);

		Field[] declaredFields = FundraiserDonation.class.getDeclaredFields();

		for (Field field : declaredFields) {
			field.setAccessible(true);
			try {
				Object value = field.get(partialDonation);
				if (value != null) {
					field.set(donation, value);
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException("error updating fundraiser donation", e);
			}
		}

		FundraiserDonation updated = this.donationRepository.save(donation);
		return this.modelMapper.map(updated, FundraiserDonationDto.class);
	}

	@Override
	public void deleteDonation(Long fDId) {
		FundraiserDonation donation = this.donationRepository.findById(fDId)
				.orElseThrow(() -> new ResourceNotFoundException("Donation", "Id", fDId));

		this.donationRepository.delete(donation);
	}

	@Override
	public List<FundraiserDonationDto> getAllDonations() {

		List<FundraiserDonation> all = this.donationRepository.findAll();

		List<FundraiserDonationDto> donationDtos = new ArrayList<>();
		for (FundraiserDonation donation : all) {
			donationDtos.add(this.modelMapper.map(donation, FundraiserDonationDto.class));
		}
		return donationDtos;
	}

	@Override
	public FundraiserDonationDto getDonationById(Long fDId) {
		FundraiserDonation donation = this.donationRepository.findById(fDId)
				.orElseThrow(() -> new ResourceNotFoundException("Donation", "Id", fDId));

		return this.modelMapper.map(donation, FundraiserDonationDto.class);
	}

	@Override
	public List<FundraiserDonationDto> getAllDonationsByFundraiser(Long fId) {

		Fundraiser fundraiser = this.fundraiserRepository.findById(fId)
				.orElseThrow(() -> new ResourceNotFoundException("Fundraiser", "Id", fId));

		List<FundraiserDonation> allByFundraiser = this.donationRepository.findByFundraiser(fundraiser);

		List<FundraiserDonationDto> donationDtos = new ArrayList<>();
		for (FundraiserDonation donation : allByFundraiser) {
			donationDtos.add(this.modelMapper.map(donation, FundraiserDonationDto.class));
		}
		return donationDtos;
	}

	@Override
	public List<FundraiserDonationDto> getAllDonationsByDonor(Long dId) {

		User user = this.userRepository.findById(dId)
				.orElseThrow(() -> new ResourceNotFoundException("User", "Id", dId));

		List<FundraiserDonation> allByDonor = this.donationRepository.findByDonor(user);

		List<FundraiserDonationDto> donationDtos = new ArrayList<>();
		for (FundraiserDonation donation : allByDonor) {
			donationDtos.add(this.modelMapper.map(donation, FundraiserDonationDto.class));
		}
		return donationDtos;
	}

}
