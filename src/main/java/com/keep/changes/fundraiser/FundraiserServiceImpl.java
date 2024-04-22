package com.keep.changes.fundraiser;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.keep.changes.category.Category;
import com.keep.changes.category.CategoryRepository;
import com.keep.changes.exception.ApiException;
import com.keep.changes.exception.ResourceNotFoundException;
import com.keep.changes.file.FileService;
import com.keep.changes.user.User;
import com.keep.changes.user.UserRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

@Service
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

	@Value("${fundraiser-cover.images}")
	private String coverImagePath;

	@Value("${fundraiser-profile.default}")
	private String DEFAULT_DISPLAY_IMAGE;

	@Value("${fundraiser-profile.default}")
	private String DEFAULT_COVER_IMAGE;

//	create
	@Override
	@Transactional
	public FundraiserDto createFundraiser(FundraiserDto fundraiserDto) {

		String loggedInUser = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = this.userRepository.findByEmail(loggedInUser)
				.orElseThrow(() -> new ResourceNotFoundException("User", "Username", loggedInUser));

		Fundraiser fundraiser = this.modelMapper.map(fundraiserDto, Fundraiser.class);
		fundraiser.setPostedBy(user);

		Fundraiser saved = this.fundraiserRepository.save(fundraiser);

		return this.modelMapper.map(saved, FundraiserDto.class);
	}

//	put update
	@Override
	@Transactional
	public FundraiserDto putUpdateFundraiser(Long fId, FundraiserDto fd) {
		Fundraiser fundraiser = this.fundraiserRepository.findById(fId)
				.orElseThrow(() -> new ResourceNotFoundException("Fundraiser", "Id", fId));

		fundraiser.putUpdateFundraiser(fId, fd.getFundraiserTitle(), fd.getFundraiserDescription(), fd.getCause(),
				fd.getRaiseGoal(), fd.getEmail(), fd.getPhone(), fd.getEndDate(), fd.getDisplayPhoto(),
				fd.getCoverPhoto());

		this.fundraiserRepository.save(fundraiser);

		return this.modelMapper.map(fundraiser, FundraiserDto.class);
	}

//	patch update
	@Override
	@Transactional
	public FundraiserDto patchFundraiser(Long fId, FundraiserDto partialFundraiserDto) {

		Fundraiser fundraiser = this.fundraiserRepository.findById(fId)
				.orElseThrow(() -> new ResourceNotFoundException("Fundraiser", "Id", fId));

		Fundraiser partialFundraiser = this.modelMapper.map(partialFundraiserDto, Fundraiser.class);

		Field[] declaredFields = Fundraiser.class.getDeclaredFields();
		for (Field field : declaredFields) {
			field.setAccessible(true);
			try {
				Object value = field.get(partialFundraiser);
				if (value != null) {
					System.out.println("2");
					System.out.println(field + " : field , value : " + value);

					if (field.getName().equals("displayPhoto")) {
						this.hasPreviousDisplay(fundraiser);
					}

					if (field.getName().equals("coverPhoto")) {
						this.hasPreviousCover(fundraiser);
					}

					field.set(fundraiser, value);
				}

			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new ApiException("error updating fundraiser", HttpStatus.BAD_REQUEST, false);
			}
		}
		System.out.println(fundraiser);
		System.out.println("second last");
		this.fundraiserRepository.save(fundraiser);
		System.out.println("finally");
		return this.modelMapper.map(fundraiser, FundraiserDto.class);
	}

//	delete
	@Override
	@Transactional
	public void deleteFundraiser(Long fId) {
		Fundraiser fundraiser = this.fundraiserRepository.findById(fId)
				.orElseThrow(() -> new ResourceNotFoundException("Fundraiser", "Id", fId));

		try {
			this.hasPreviousDisplay(fundraiser);
			this.hasPreviousCover(fundraiser);
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

	@Override
	@Transactional
	public boolean deleteCover(@Valid Long fId) {

		Fundraiser fundraiser = this.fundraiserRepository.findById(fId)
				.orElseThrow(() -> new ResourceNotFoundException("Fundraiser", "Id", fId));

		if (!this.hasPreviousCover(fundraiser)) {
			return false;
		}

		fundraiser.setCoverPhoto(DEFAULT_COVER_IMAGE);
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

//	by cause
	@Override
	@Transactional
	public List<FundraiserDto> getFundraisersByCause(String cause) {

		List<Fundraiser> fundraisers = this.fundraiserRepository.findByCauseContaining(cause);

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

//	delete if previous cover exists
	private boolean hasPreviousCover(Fundraiser fundraiser) {

		boolean isDeleted = false;

		if (fundraiser.getCoverPhoto() != null && !fundraiser.getCoverPhoto().equals("")
				&& !fundraiser.getCoverPhoto().equals(this.DEFAULT_COVER_IMAGE)) {

			try {
				this.fileService.deleteFile(coverImagePath, fundraiser.getCoverPhoto());
				isDeleted = true;
			} catch (IOException e) {
				throw new ApiException("OOPS!! Something went wrong. Could not update cover image.",
						HttpStatus.BAD_REQUEST, false);
			}

			if (isDeleted == false) {
				throw new ApiException("OOPS!! Something went wrong. Could not update cover image.",
						HttpStatus.BAD_REQUEST, false);
			}
		}
		return isDeleted;
	}
}
