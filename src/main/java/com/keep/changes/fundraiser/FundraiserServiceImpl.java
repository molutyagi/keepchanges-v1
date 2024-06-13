package com.keep.changes.fundraiser;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.keep.changes.category.Category;
import com.keep.changes.category.CategoryRepository;
import com.keep.changes.exception.ApiException;
import com.keep.changes.exception.ResourceNotFoundException;
import com.keep.changes.file.FileService;
import com.keep.changes.fundraiser.document.FundraiserDocument;
import com.keep.changes.fundraiser.photo.Photo;
import com.keep.changes.user.User;
import com.keep.changes.user.UserRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Service
@Transactional
public class FundraiserServiceImpl implements FundraiserService {

	@Autowired
	private FundraiserRepository fundraiserRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private FileService fileService;

	@Value("${fundraiser-profile.images}")
	private String displayImagePath;

	@Value("${fundraiser-profile.default}")
	private String DEFAULT_DISPLAY_IMAGE;

//	create
	@Override
	@Transactional
	public FundraiserDto createFundraiser(FundraiserDto fundraiserDto) {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String loggedInUser = authentication.getName();

		User user = this.userRepository.findByEmail(loggedInUser)
				.orElseThrow(() -> new ResourceNotFoundException("User", "Username", loggedInUser));

		Fundraiser fundraiser = this.modelMapper.map(fundraiserDto, Fundraiser.class);
		fundraiser.setPostedBy(user);

		if (authentication.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
			fundraiser.setIsActive(true);
			fundraiser.setStatus(FundraiserStatus.OPEN);
			fundraiser.setApproval(AdminApproval.APPROVED);
			fundraiser.setAdminRemarks("This fundraiser is created by the keep changes team itself.");
			fundraiser.setIsReviewed(true);
		} else {
			fundraiser.setIsActive(false);
			fundraiser.setStatus(FundraiserStatus.INACTIVE);
			fundraiser.setApproval(AdminApproval.PENDING);
			fundraiser.setIsReviewed(false);
		}

		Fundraiser saved = this.fundraiserRepository.save(fundraiser);

		return this.modelMapper.map(saved, FundraiserDto.class);
	}

//	put update
	@Override
	@Transactional
	public FundraiserDto putUpdateFundraiser(Long fId, FundraiserDto fd) {
		Fundraiser fundraiser = this.fundraiserRepository.findById(fId)
				.orElseThrow(() -> new ResourceNotFoundException("Fundraiser", "Id", fId));

		fundraiser.putUpdateFundraiser(fId, fd.getFundraiserTitle(), fd.getFundraiserDescription(), fd.getBeneficiary(),
				fd.getRaiseGoal(), fd.getEmail(), fd.getPhone(), fd.getEndDate(), fd.getDisplayPhoto(),
				fd.getCoverPhoto());

		Fundraiser updated = this.fundraiserRepository.save(fundraiser);

		return this.modelMapper.map(updated, FundraiserDto.class);
	}

//	patch update
	@Override
	@Transactional
	public FundraiserDto patchFundraiser(Long fId, FundraiserDto partialFundraiserDto) {

		System.out.println("patch fundraiser");
		Fundraiser fundraiser = this.fundraiserRepository.findById(fId)
				.orElseThrow(() -> new ResourceNotFoundException("Fundraiser", "Id", fId));
		System.out.println(partialFundraiserDto);
		Fundraiser partialFundraiser = this.modelMapper.map(partialFundraiserDto, Fundraiser.class);

		Field[] declaredFields = Fundraiser.class.getDeclaredFields();
		for (Field field : declaredFields) {

			if (field.getName().equals("isActive")) {
				break;
			}

			System.out.println("field: " + field);
			field.setAccessible(true);
			try {
				Object value = field.get(partialFundraiser);
				if (value != null) {
					System.out.println("field: " + field);
					System.out.println("value: " + value);

					if (field.getName().equals("displayPhoto")) {
						this.hasPreviousDisplay(fundraiser);
					}

					field.set(fundraiser, value);
				}

			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new ApiException("error updating fundraiser", HttpStatus.BAD_REQUEST, false);
			}
		}
		Fundraiser updated = this.fundraiserRepository.save(fundraiser);
		return this.modelMapper.map(updated, FundraiserDto.class);
	}

//	delete
	@Override
	@Transactional(rollbackOn = Exception.class)
	public void deleteFundraiser(Long fId) {
		Fundraiser fundraiser = this.fundraiserRepository.findById(fId)
				.orElseThrow(() -> new ResourceNotFoundException("Fundraiser", "Id", fId));

		try {
//			delete display image from directory
			this.hasPreviousDisplay(fundraiser);

//			delete photos from directory
			for (Photo photo : fundraiser.getPhotos()) {
				this.fileService.deleteFile(null, photo.getPhotoUrl());
			}

//			delete documents from directory
			for (FundraiserDocument document : fundraiser.getDocuments()) {
				this.fileService.deleteFile(null, document.getDocumentUrl());
			}

//			delete fundraiser from db
			this.fundraiserRepository.delete(fundraiser);
		} catch (Exception e) {
			throw new ApiException("OOPS!! Something went wrong. Could not delete fundraiser.",
					HttpStatus.INTERNAL_SERVER_ERROR, false);
		}
	}

	@Override
	@Transactional
	public boolean deleteDisplay(@Valid Long fId) {
		Fundraiser fundraiser = this.fundraiserRepository.findById(fId)
				.orElseThrow(() -> new ResourceNotFoundException("Fundraiser", "Id", fId));

		if (!this.hasPreviousDisplay(fundraiser)) {
			return false;
		}

		fundraiser.setDisplayPhoto(DEFAULT_DISPLAY_IMAGE);
		this.fundraiserRepository.save(fundraiser);
		return true;
	}

//	get
	@Override
	@Transactional
	public List<FundraiserDto> getLatestFundraiser() {
		List<Fundraiser> fundraisers = this.fundraiserRepository.findLatestFundraisers();
		System.out.println("service impl");
		return this.fundraiserToDto(fundraisers);
	}

	@Override
	@Transactional
	public List<FundraiserDto> getAllActiveFundraisers() {
		List<Fundraiser> fundraisers = this.fundraiserRepository.findAllActiveFundraisers();
		return this.fundraiserToDto(fundraisers);
	}

//	by id
	@Override
	@Transactional
	public FundraiserDto getFundraiserById(Long fId) {
		Fundraiser fundraiser = this.fundraiserRepository.findById(fId)
				.orElseThrow(() -> new ResourceNotFoundException("Fundraiser", "Id", fId));
		return this.modelMapper.map(fundraiser, FundraiserDto.class);
	}

//	get all
	@Override
	@Transactional
	public List<FundraiserDto> getAllFundraisers() {
		List<Fundraiser> fundraisers = this.fundraiserRepository.findAll();

		return fundraiserToDto(fundraisers);

	}

//	by email
	@Override
	@Transactional
	public List<FundraiserDto> getFundraiserByEmail(String email) {
		List<Fundraiser> fundraisers = this.fundraiserRepository.findByEmail(email);

		return fundraiserToDto(fundraisers);
	}

//	by phone
	@Override
	@Transactional
	public List<FundraiserDto> getFundraiserByPhone(String phone) {
		List<Fundraiser> fundraisers = this.fundraiserRepository.findByPhone(phone);

		return fundraiserToDto(fundraisers);
	}

//	by title containing
	@Override
	@Transactional
	public List<FundraiserDto> getFundraisersByTitle(String title) {
		List<Fundraiser> fundraisers = this.fundraiserRepository.findByFundraiserTitleContaining(title);

		return fundraiserToDto(fundraisers);
	}

//	by category
	@Override
	@Transactional
	public List<FundraiserDto> getFundraisersByCategory(Long categoryId) {

		Category category = this.categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category", "Id", categoryId));

		List<Fundraiser> fundraisers = this.fundraiserRepository.findByCategory(category);

		return fundraiserToDto(fundraisers);
	}

//	by poster
	@Override
	@Transactional
	public List<FundraiserDto> getFundraisersByPoster(String username) {

		List<User> users = this.userRepository.findByNameContaining(username)
				.orElseThrow(() -> new ResourceNotFoundException("User", "Username", username));

		List<Fundraiser> fundraisers = new ArrayList<>();
		for (User user : users) {

			List<Fundraiser> fundraisersByUser = this.fundraiserRepository.findByPostedBy(user);

			fundraisers.addAll(fundraisersByUser);
		}

		return fundraiserToDto(fundraisers);
	}

	@Override
	@Transactional
	public List<FundraiserDto> getFundraisersByPosterId(Long pId) {
		User user = this.userRepository.findById(pId)
				.orElseThrow(() -> new ResourceNotFoundException("User", "Id", pId));

		List<Fundraiser> fundraisers = this.fundraiserRepository.findByPostedBy(user);

		return fundraiserToDto(fundraisers);
	}

	@Override
	@Transactional
	public List<FundraiserDto> getActiveFundraisersByPosterId(Long pId) {
		User user = this.userRepository.findById(pId)
				.orElseThrow(() -> new ResourceNotFoundException("User", "Id", pId));

		List<Fundraiser> fundraisers = this.fundraiserRepository.findActiveByPostedBy(user);

		return fundraiserToDto(fundraisers);
	}

	private List<FundraiserDto> fundraiserToDto(List<Fundraiser> fundraisers) {

		List<FundraiserDto> fundraiserDtos = new ArrayList<FundraiserDto>();
		for (Fundraiser fundraiser : fundraisers) {
			fundraiserDtos.add(this.modelMapper.map(fundraiser, FundraiserDto.class));
		}

		return fundraiserDtos;
	}

//	delete if previous display exists
	private boolean hasPreviousDisplay(Fundraiser fundraiser) {

		boolean isDeleted = false;

		if (fundraiser.getDisplayPhoto() != null && !fundraiser.getDisplayPhoto().equals("")
				&& !fundraiser.getDisplayPhoto().equals(this.DEFAULT_DISPLAY_IMAGE)) {

			try {
				this.fileService.deleteFile(displayImagePath, fundraiser.getDisplayPhoto());
				isDeleted = true;
			} catch (IOException e) {
				throw new ApiException("OOPS!! Something went wrong. Could not update display image.",
						HttpStatus.BAD_REQUEST, false);
			}

			if (isDeleted == false) {
				throw new ApiException("OOPS!! Something went wrong. Could not update display image.",
						HttpStatus.BAD_REQUEST, false);
			}
		}
		return isDeleted;
	}

	@Override
	@Transactional
	public void fundraiserAdminService(@Valid Long fId, String adminRemarks, AdminApproval adminStatus) {

		Fundraiser fundraiser = this.fundraiserRepository.findById(fId)
				.orElseThrow(() -> new ResourceNotFoundException("Fundraiser", "Id", fId));

		fundraiser.setAdminRemarks(adminRemarks);
		fundraiser.setApproval(adminStatus);
		if (adminStatus.equals(AdminApproval.APPROVED)) {
			fundraiser.setIsActive(true);
			fundraiser.setStatus(FundraiserStatus.OPEN);
		}

		if (adminStatus.equals(AdminApproval.PENDING)) {
			fundraiser.setIsActive(false);
			fundraiser.setStatus(FundraiserStatus.INACTIVE);
		}

		if (adminStatus.equals(AdminApproval.DISAPPROVED)) {
			fundraiser.setIsActive(false);
			fundraiser.setStatus(FundraiserStatus.CANCELLED);
		}
		fundraiser.setIsReviewed(true);
		this.fundraiserRepository.save(fundraiser);

	}

//	Admin dashboard
	@Override
	@Transactional
	public Double sumOfRaised() {
		return this.fundraiserRepository.sumOfRaised();
	}

	@Override
	@Transactional
	public Double sumOfRaiseGoal() {
		return this.fundraiserRepository.sumOfRaiseGoal();
	}

	@Override
	@Transactional
	public Long totalFundraisers() {
		return this.fundraiserRepository.count();
	}

	@Override
	@Transactional
	public Long totalActiveFundraisers() {
		return this.fundraiserRepository.countAllByIsActive(true);
	}

	@Override
	public List<FundraiserDto> findByIsReviewedFalse() {
		return this.fundraiserToDto(this.fundraiserRepository.findByIsReviewedFalse());
	}
}
