package com.keep.changes.fundraiser;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
		fundraiser.setRaised(0.0);

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
//	by id
	@Override
	@Transactional
	public FundraiserDto getFundraiserById(Long fId) {
		Fundraiser fundraiser = this.fundraiserRepository.findById(fId)
				.orElseThrow(() -> new ResourceNotFoundException("Fundraiser", "Id", fId));
		return this.modelMapper.map(fundraiser, FundraiserDto.class);
	}

//	by id
	@Override
	@Transactional
	public FundraiserDetailsResponse getFundraiserById1(Long fId) {
		Fundraiser fundraiser = this.fundraiserRepository.findById(fId)
				.orElseThrow(() -> new ResourceNotFoundException("Fundraiser", "Id", fId));
		return this.modelMapper.map(fundraiser, FundraiserDetailsResponse.class);
	}

//	get all
	@Override
	@Transactional
	public FundraiserCardResponse getAllFundraisers(Integer pageNumber, Integer pageSize) {

		Pageable pageable = PageRequest.of(pageNumber, pageSize);

		Page<Fundraiser> page = this.fundraiserRepository.findAll(pageable);

		return this.pageFundraiserToDto(page);
	}

//	get all active
	@Override
	@Transactional
	public FundraiserCardResponse getAllActiveFundraisers(Integer pageNumber, Integer pageSize) {

		Pageable pageable = PageRequest.of(pageNumber, pageSize);

		Page<Fundraiser> page = this.fundraiserRepository.findAllByIsActiveTrue(pageable);

		return this.pageFundraiserToDto(page);
	}

//	get active 100
	@Override
	@Transactional
	public FundraiserCardResponse getActive100Fundraisers(Integer pageNumber, Integer pageSize) {

		Pageable pageable = PageRequest.of(pageNumber, pageSize);

		Page<Fundraiser> page = this.fundraiserRepository.findAllByIsActiveTrue(pageable);

		return this.pageFundraiserToDto(page);
	}

//	get latest 6
	@Override
	@Transactional
	public FundraiserCardResponse getLatestFundraiser() {
		List<Fundraiser> fundraisers = this.fundraiserRepository.findTop6ByIsActiveTrueOrderByIdDesc();
		System.out.println("service impl");
		return this.fundraiserToResponseDto(fundraisers);
	}

//	by email
	@Override
	@Transactional
	public FundraiserCardResponse getFundraiserByEmail(String email, Integer pageNumber, Integer pageSize) {

		Pageable pageable = PageRequest.of(pageNumber, pageSize);

		Page<Fundraiser> page = this.fundraiserRepository.findAllByIsActiveTrueAndEmailContaining(email, pageable);

		return this.pageFundraiserToDto(page);
	}

//	by phone
	@Override
	@Transactional
	public FundraiserCardResponse getFundraiserByPhone(String phone, Integer pageNumber, Integer pageSize) {

		Pageable pageable = PageRequest.of(pageNumber, pageSize);

		Page<Fundraiser> page = this.fundraiserRepository.findAllByIsActiveTrueAndPhoneContaining(phone, pageable);

		return this.pageFundraiserToDto(page);
	}

//	by title containing
	@Override
	@Transactional
	public FundraiserCardResponse getFundraisersByTitle(String title, Integer pageNumber, Integer pageSize) {

		Pageable pageable = PageRequest.of(pageNumber, pageSize);

		Page<Fundraiser> page = this.fundraiserRepository.findAllByIsActiveTrueAndFundraiserTitleContaining(title,
				pageable);

		return this.pageFundraiserToDto(page);
	}

//	by category
	@Override
	@Transactional
	public FundraiserCardResponse getFundraisersByCategory(Long categoryId, Integer pageNumber, Integer pageSize) {

		Category category = this.categoryRepository.findById(categoryId)
				.orElseThrow(() -> new ResourceNotFoundException("Category", "Id", categoryId));

		Pageable pageable = PageRequest.of(pageNumber, pageSize);

		Page<Fundraiser> page = this.fundraiserRepository.findAllByIsActiveTrueAndCategory(category, pageable);

		return this.pageFundraiserToDto(page);
	}

//	by multiple categories
	@Override
	@Transactional
	public FundraiserCardResponse getFundraisersByCategories(Long categoryIds[], Integer pageNumber, Integer pageSize) {

		List<Category> categories = new ArrayList<>();

		for (Long categoryId : categoryIds) {

			Category category = this.categoryRepository.findById(categoryId)
					.orElseThrow(() -> new ResourceNotFoundException("Category", "Id", categoryId));
			categories.add(category);
		}

		Pageable pageable = PageRequest.of(pageNumber, pageSize);

		Page<Fundraiser> page = this.fundraiserRepository.findAllByIsActiveTrueAndCategoryIn(categories, pageable);

		return this.pageFundraiserToDto(page);
	}

//	by poster name
	@Override
	@Transactional
	public FundraiserCardResponse getFundraisersByPoster(String username, Integer pageNumber, Integer pageSize) {

		Pageable pageable = PageRequest.of(pageNumber, pageSize);

		List<User> users = this.userRepository.findByNameContaining(username)
				.orElseThrow(() -> new ResourceNotFoundException("User", "Username", username));

		System.out.println(users.size());

		Page<Fundraiser> fundraisers = this.fundraiserRepository.findAllByIsActiveTrueAndPostedByIn(users, pageable);

		return this.pageFundraiserToDto(fundraisers);

	}

//	get all by poster id
	@Override
	@Transactional
	public FundraiserCardResponse getFundraisersByPosterId(Long pId, Integer pageNumber, Integer pageSize) {
		User user = this.userRepository.findById(pId)
				.orElseThrow(() -> new ResourceNotFoundException("User", "Id", pId));

		Pageable pageable = PageRequest.of(pageNumber, pageSize);

		Page<Fundraiser> page = this.fundraiserRepository.findByPostedBy(user, pageable);

		return this.pageFundraiserToDto(page);
	}

//	get active by poster id
	@Override
	@Transactional
	public FundraiserCardResponse getActiveFundraisersByPosterId(Long pId, Integer pageNumber, Integer pageSize) {
		User user = this.userRepository.findById(pId)
				.orElseThrow(() -> new ResourceNotFoundException("User", "Id", pId));

		Pageable pageable = PageRequest.of(pageNumber, pageSize);

		Page<Fundraiser> page = this.fundraiserRepository.findAllByIsActiveTrueAndPostedBy(user, pageable);

		return this.pageFundraiserToDto(page);
	}

	// ----------------- ADMIN SERVICES --------------- //
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
		return this.fundraiserRepository.sumRaisedByIsActive();
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
	@Transactional
	public FundraiserCardResponse findByIsReviewedFalse(Integer pageNumber, Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNumber, pageSize);

		Page<Fundraiser> page = this.fundraiserRepository.findByIsReviewedFalse(pageable);

		return this.pageFundraiserToDto(page);
	}
//	Private methods

	private List<FundraiserDto> fundraiserToDto(List<Fundraiser> fundraisers) {

		List<FundraiserDto> fundraiserDtos = new ArrayList<FundraiserDto>();
		for (Fundraiser fundraiser : fundraisers) {
			fundraiserDtos.add(this.modelMapper.map(fundraiser, FundraiserDto.class));
		}

		return fundraiserDtos;
	}

	private FundraiserCardResponse fundraiserToResponseDto(List<Fundraiser> fundraisers) {

		List<FundraiserCardDto> fundraiserDtos = new ArrayList<FundraiserCardDto>();
		for (Fundraiser fundraiser : fundraisers) {
			fundraiserDtos.add(this.modelMapper.map(fundraiser, FundraiserCardDto.class));
		}

		FundraiserCardResponse cardResponse = new FundraiserCardResponse();
		cardResponse.setFundraisers(fundraiserDtos);
		cardResponse.setPageNo(0);
		cardResponse.setPageSize(100);
		cardResponse.setTotalElements(fundraisers.size());
		cardResponse.setTotalPages(fundraisers.size() / 100);
		cardResponse.setLastPage(false);

		return cardResponse;
	}

	private FundraiserCardResponse pageFundraiserToDto(Page<Fundraiser> fundraisers) {

//		List<Fundraiser> content = fundraisers.getContent();

		List<FundraiserCardDto> fundraiserDtos = new ArrayList<FundraiserCardDto>();
		for (Fundraiser fundraiser : fundraisers) {
			fundraiserDtos.add(this.modelMapper.map(fundraiser, FundraiserCardDto.class));
		}

		FundraiserCardResponse cardResponse = new FundraiserCardResponse();
		cardResponse.setFundraisers(fundraiserDtos);
		cardResponse.setPageNo(fundraisers.getNumber());
		cardResponse.setPageSize(fundraisers.getSize());
		cardResponse.setTotalElements(fundraisers.getTotalElements());
		cardResponse.setTotalPages(fundraisers.getTotalPages());
		cardResponse.setLastPage(fundraisers.isLast());

		return cardResponse;
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
}
