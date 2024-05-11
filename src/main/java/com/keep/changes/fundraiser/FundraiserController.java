package com.keep.changes.fundraiser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.keep.changes.account.AccountDto;
import com.keep.changes.account.AccountService;
import com.keep.changes.category.CategoryDto;
import com.keep.changes.category.CategoryService;
import com.keep.changes.exception.ApiException;
import com.keep.changes.file.FileService;
import com.keep.changes.fundraiser.document.FundraiserDocumentDto;
import com.keep.changes.fundraiser.document.FundraiserDocumentService;
import com.keep.changes.fundraiser.photo.FundraiserPhotoService;
import com.keep.changes.fundraiser.photo.PhotoDto;
import com.keep.changes.payload.response.ApiResponse;
import com.keep.changes.user.UserDto;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/fundraisers")
public class FundraiserController {

	@Autowired
	private FundraiserService fundraiserService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private AccountService accountService;

	@Autowired
	private FundraiserPhotoService photoService;

	@Autowired
	private FundraiserDocumentService documentService;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private FileService fileService;

	@Value("${fundraiser-profile.images}")
	private String displayImagePath;

	@Value("${fundraiser.images}")
	private String fundraiserImages;

	@Value("${fundraiser.documents}")
	private String fundraiserDocuments;

	@Value("${fundraiser-profile.default}")
	private String DEFAULT_DISPLAY_IMAGE;

	// Fundraiser Controllers
	// add complete fundraiser in a single request
	@PostMapping(value = { "add", "add/" })
	public ResponseEntity<?> createFundraiser(
			@Valid @RequestParam(value = "displayImage", required = false) MultipartFile displayImage,
			@RequestParam(value = "fundraiserData", required = true) String fundraiserData,
			@RequestParam(value = "categoryId", required = true) Long categoryId) {

//		verify and validate images
		if (!this.verifyImage(displayImage)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Select valid image");
		}

		FundraiserDto fundraiserDto = new FundraiserDto();
		FundraiserDto createdFundraiser = null;
		String displayImageName;

		System.out.println(fundraiserData);

		// set json data to dto
		try {
			fundraiserDto = this.objectMapper.readValue(fundraiserData, FundraiserDto.class);
		} catch (JsonProcessingException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Request!");
		}

		// save and set display image
		try {
			displayImageName = this.fileService.uploadImage(displayImagePath, displayImage);
			fundraiserDto.setDisplayPhoto(displayImageName);
		} catch (IOException e) {
			throw new ApiException("OOPS!! Something went wrong. Could not create fundraiser.", HttpStatus.BAD_REQUEST,
					false);
		}

		// save fundraiser
		try {
			// get category
			CategoryDto categoryDto = this.categoryService.getById(categoryId);
			fundraiserDto.setCategory(categoryDto);
			createdFundraiser = this.fundraiserService.createFundraiser(fundraiserDto);
		} catch (Exception e) {
			try {
				this.fileService.deleteFile(displayImagePath, displayImageName);
			} catch (IOException e1) {
				throw new ApiException("OOPS!! Something went wrong. Could not create fundraiser.",
						HttpStatus.BAD_REQUEST, false);
			}

		}
		return ResponseEntity.ok(createdFundraiser);
	}

	// update complete fundraiser in a single request
	@PatchMapping(value = { "fundraiser_{fId}", "fundraiser_{fId}/" })
	@PreAuthorize("@fundraiserController.authenticateUser(#fId, authentication.principal.id, hasRole('ADMIN'))")
	public ResponseEntity<?> updateFundraiser(@Valid @PathVariable Long fId,
			@RequestParam(value = "displayImage", required = false) MultipartFile displayImage,
			@RequestParam(value = "fundraiserData", required = false) String fundraiserData,
			@RequestParam(value = "categoryId", required = false) Long categoryId,
			@RequestParam(value = "accountId", required = false) Long accountId,
			@RequestParam(value = "accountData", required = false) String accountData) {

		FundraiserDto fundraiserDto = new FundraiserDto();
		FundraiserDto updatedFundraiser = null;
		String displayImageName = null;

		// set json data to dto if exists
		if (fundraiserData != null) {
			try {
				fundraiserDto = this.objectMapper.readValue(fundraiserData, FundraiserDto.class);
			} catch (JsonProcessingException e) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Request!");
			}
		}

		// save and set display image
		if (this.verifyImage(displayImage)) {
			try {
				displayImageName = this.fileService.uploadImage(displayImagePath, displayImage);
				fundraiserDto.setDisplayPhoto(displayImageName);
			} catch (IOException e) {
				throw new ApiException("OOPS!! Something went wrong. Could not create fundraiser.",
						HttpStatus.BAD_REQUEST, false);
			}
		}

		if (categoryId != null) {
			// get category
			CategoryDto categoryDto = this.categoryService.getById(categoryId);
			fundraiserDto.setCategory(categoryDto);
		}

		if (accountId != null) {
			AccountDto accountDto = this.accountService.getAccountById(accountId);
			fundraiserDto.setAccount(accountDto);
		}

		if (accountData != null) {
			try {
				AccountDto accountDto = this.objectMapper.readValue(accountData, AccountDto.class);
				AccountDto account = this.accountService.addAccount(accountDto);
				fundraiserDto.setAccount(account);
			} catch (JsonProcessingException e) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Request!");
			}
		}

		// save fundraiser
		try {
			updatedFundraiser = this.fundraiserService.patchFundraiser(fId, fundraiserDto);
		} catch (Exception e) {
			try {
				this.fileService.deleteFile(displayImagePath, displayImageName);
			} catch (IOException e1) {
				throw new ApiException("OOPS!! Something went wrong. Could not update fundraiser.",
						HttpStatus.BAD_REQUEST, false);
			}

			throw new ApiException("OOPS something went wrong could not update fundraiser",
					HttpStatus.INTERNAL_SERVER_ERROR, false);
		}
		return ResponseEntity.ok(updatedFundraiser);
	}

	// patch fundraiser details
	@PatchMapping(value = { "fundraiser/update_{fId}", "fundraiser/update_{fId}/" })
	@PreAuthorize("@fundraiserController.authenticateUser(#fId, authentication.principal.id, hasRole('ADMIN'))")
	public ResponseEntity<FundraiserDto> patchUpdateFundraiser(@Valid @PathVariable Long fId,
			@RequestBody FundraiserDto fundraiserDto) {
		return ResponseEntity.ok(this.fundraiserService.patchFundraiser(fId, fundraiserDto));
	}

	// Put Update fundraiser
	@PutMapping("fundraiser_{fId}")
	@PreAuthorize("@fundraiserController.authenticateUser(#fId, authentication.principal.id, hasRole('ADMIN'))")
	public ResponseEntity<FundraiserDto> putUpdateFundraiser(@Valid @PathVariable Long fId,
			@Valid @RequestBody FundraiserDto fundraiserDto) {
		System.out.println(fundraiserDto);
		return ResponseEntity.ok(this.fundraiserService.putUpdateFundraiser(fId, fundraiserDto));
	}

	// update category
	@PatchMapping("fundraiser_{fId}/category")
	@PreAuthorize("@fundraiserController.authenticateUser(#fId, authentication.principal.id, hasRole('ADMIN'))")
	public ResponseEntity<FundraiserDto> patchFundraiserCategory(@Valid @PathVariable Long fId,
			@RequestParam("categoryId") Long categoryId) {

		CategoryDto categoryDto = this.categoryService.getById(categoryId);

		FundraiserDto fundraiserDto = new FundraiserDto();
		fundraiserDto.setCategory(categoryDto);
		return ResponseEntity.ok(this.fundraiserService.patchFundraiser(fId, fundraiserDto));
	}

	//// add fundraiser
	// @PostMapping("add")
	// public ResponseEntity<FundraiserDto> createFundraiser(@Valid @RequestBody
	//// FundraiserDto fundraiserDto) {
	//
	// FundraiserDto savedFundraiser =
	//// this.fundraiserService.createFundraiser(fundraiserDto);
	//
	// return new ResponseEntity<FundraiserDto>(savedFundraiser,
	//// HttpStatus.CREATED);
	// }

	// Delete
	@DeleteMapping("fundraiser_{fId}")
	@PreAuthorize("@fundraiserController.authenticateUser(#fId, authentication.principal.id, hasRole('ADMIN'))")
	public ResponseEntity<ApiResponse> deleteFundraiser(@Valid @PathVariable Long fId) {
		System.out.println("controller");
		this.fundraiserService.deleteFundraiser(fId);
		return ResponseEntity.ok(new ApiResponse("Fundraiser Deleted Successfully!!", true));
	}

	// -------------------------Fundraiser Get Controllers-------------------------
	// By Id
	@GetMapping(value = { "fundraiser_{fId}", "fundraiser_{fId}/", "fundraiser/{fId}", "fundraiser/{fId}/" })
	public ResponseEntity<FundraiserDto> getById(@PathVariable long fId) {

		return ResponseEntity.ok(this.fundraiserService.getFundraiserById(fId));
	}

	// Get All
	@GetMapping(value = { "", "/", "getall", "getall/" })
	public ResponseEntity<List<FundraiserDto>> getAll() {
		return ResponseEntity.ok(this.fundraiserService.getAllFundraisers());
	}

	// Get By Email
	@GetMapping(value = { "email/{email}", "email/{email}/", "getall/email/{email}", "getall/email/{email}/" })
	public ResponseEntity<List<FundraiserDto>> getByEmail(@Valid @PathVariable String email) {

		return ResponseEntity.ok(this.fundraiserService.getFundraiserByEmail(email));
	}

	// Get By Phone
	@GetMapping(value = { "phone/{phone}", "phone/{phone}/", "getall/phone/{phone}", "getall/phone/{phone}/" })
	public ResponseEntity<List<FundraiserDto>> getByPhone(@Valid @PathVariable String phone) {

		return ResponseEntity.ok(this.fundraiserService.getFundraiserByPhone(phone));
	}

	// Get By Title
	@GetMapping(value = { "title/{title}", "title/{title}/", "getall/title/{title}", "getall/title/{title}/" })
	public ResponseEntity<List<FundraiserDto>> getByTitle(@Valid @PathVariable String title) {

		return ResponseEntity.ok(this.fundraiserService.getFundraisersByTitle(title));
	}

	// Get By Poster
	@GetMapping(value = { "postedby/{username}", "postedby/{username}/", "getall/postedby/{username}",
			"getall/postedby/{username}/" })
	public ResponseEntity<List<FundraiserDto>> getByPoster(@Valid @PathVariable String username) {

		return ResponseEntity.ok(this.fundraiserService.getFundraisersByPoster(username));
	}

	// Get By Cause
	@GetMapping(value = { "cause/{cause}", "cause/{cause}/", "getall/cause/{cause}", "getall/cause/{cause}/" })
	public ResponseEntity<List<FundraiserDto>> getByCause(@Valid @PathVariable String cause) {

		return ResponseEntity.ok(this.fundraiserService.getFundraisersByCause(cause));
	}

	// Get By Category
	@GetMapping(value = { "category/{categoryId}", "category/{categoryId}/", "getall/category/{categoryId}",
			"getall/category/{categoryId}/" })
	public ResponseEntity<List<FundraiserDto>> getByCategory(@Valid @PathVariable Long categoryId) {

		return ResponseEntity.ok(this.fundraiserService.getFundraisersByCategory(categoryId));
	}

	// --------------------- Fundraiser Account Controllers ---------------------

	// add fundraiser account
	@PatchMapping(value = { "fundraiser_{fId}/account/add", "fundraiser_{fId}/account/add/" })
	@PreAuthorize("@fundraiserController.authenticateUser(#fId, authentication.principal.id, hasRole('ADMIN'))")
	public ResponseEntity<?> addFundraiserAccount(@Valid @PathVariable Long fId, @RequestBody AccountDto accountDto) {
		FundraiserDto fundraiserDto = new FundraiserDto();
		AccountDto addedAccount = this.accountService.addAccount(accountDto);
		fundraiserDto.setAccount(addedAccount);
		FundraiserDto updatedFundraiser = this.fundraiserService.patchFundraiser(fId, fundraiserDto);

		return ResponseEntity.ok(updatedFundraiser);
	}

	// add fundraiser account by Id
	@PatchMapping(value = { "fundraiser_{fId}/account_{aId}", "fundraiser_{fId}/account_{aId}/" })
	@PreAuthorize("@fundraiserController.authenticateUser(#fId, authentication.principal.id, hasRole('ADMIN'))")
	public ResponseEntity<?> addFundraiserAccountById(@Valid @PathVariable Long fId, @PathVariable Long aId) {
		FundraiserDto fundraiserDto = new FundraiserDto();
		AccountDto account = this.accountService.getAccountById(aId);
		fundraiserDto.setAccount(account);
		FundraiserDto updatedFundraiser = this.fundraiserService.patchFundraiser(fId, fundraiserDto);

		return ResponseEntity.ok(updatedFundraiser);
	}

	// ------------------- All Fundraiser Photos Controllers -------------------

	// ------------------- Fundraiser Display Image Controllers -------------------
	// update fundraiser display image
	@PatchMapping(value = { "fundraiser_{fId}/display", "fundraiser_{fId}/display/" })
	@PreAuthorize("@fundraiserController.authenticateUser(#fId, authentication.principal.id, hasRole('ADMIN'))")
	public ResponseEntity<?> updateDisplay(@Valid @PathVariable Long fId,
			@RequestParam("displayImage") MultipartFile displayImage) {

		if (!this.verifyImage(displayImage)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Select valid image");
		}

		FundraiserDto fundraiserDto = new FundraiserDto();
		FundraiserDto updatedFundraiser = null;
		String displayImageName = null;

		// save image in directory
		try {
			displayImageName = this.fileService.uploadImage(displayImagePath, displayImage);
		} catch (IOException e) {
			return ResponseEntity.badRequest().body("OOPS Something went wrong. Could not update display image");
		}

		// save in database
		fundraiserDto.setDisplayPhoto(displayImageName);

		try {
			updatedFundraiser = this.fundraiserService.patchFundraiser(fId, fundraiserDto);
		} catch (Exception e) {
			try {
				this.fileService.deleteFile(displayImageName, displayImageName);
			} catch (IOException e1) {
				return ResponseEntity.internalServerError()
						.body("OOPS something went wrong. Could not update display image.");
			}
			throw new ApiException("OOPS soemthing went wrong could not update display image",
					HttpStatus.INTERNAL_SERVER_ERROR, false);
		}
		return ResponseEntity.ok(updatedFundraiser);
	}

	// delete display image
	@DeleteMapping(value = { "fundraiser_{fId}/display", "fundraiser_{fId}/display/" })
	@PreAuthorize("@fundraiserController.authenticateUser(#fId, authentication.principal.id, hasRole('ADMIN'))")
	public ResponseEntity<ApiResponse> deleteDisplayImage(@Valid @PathVariable Long fId) {
		if (!this.fundraiserService.deleteDisplay(fId)) {
			return ResponseEntity.ok(new ApiResponse("Display image does not exist.", false));
		}
		return ResponseEntity.ok(new ApiResponse("Display image deleted successfully.", false));
	}

	// get display image
	@GetMapping(value = { "fundraiser_{fId}/display/{displayImageName}",
			"fundraiser_{fId}/display/{displayImageName}/" })
	public void getFundraiserDisplayImage(@Valid @PathVariable Long fId, @PathVariable String displayImageName,
			HttpServletResponse res) {

		InputStream is;

		try {
			is = this.fileService.getResource(displayImagePath, displayImageName);
		} catch (Exception e) {
			throw new ApiException("No such image exists.", HttpStatus.BAD_REQUEST, false);
		}

		res.setContentType(MediaType.IMAGE_JPEG_VALUE);
		try {
			StreamUtils.copy(is, res.getOutputStream());
		} catch (IOException e) {
			throw new ApiException("OOPS!! Something went wrong. Could not get fundraiser display image.",
					HttpStatus.INTERNAL_SERVER_ERROR, false);
		}
	}

	// ---------------------- Fundraiser Photos Controllers ----------------------

	// add photos
	@PostMapping(value = { "fundraiser_{fId}/add-photos", "fundraiser_{fId}/add-photos/" })
	@PreAuthorize("@fundraiserController.authenticateUser(#fId, authentication.principal.id, hasRole('ADMIN'))")
	public ResponseEntity<?> uploadFundraiserPhotos(@Valid @PathVariable Long fId,
			@RequestParam("images") MultipartFile[] images) {

		for (MultipartFile image : images) {
			if (!this.verifyImage(image)) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Select valid image");
			}
		}

		List<PhotoDto> allPhotos = new ArrayList<>();

		Arrays.stream(images).forEach(multipartFile -> {
			try {
				String imageName = this.fileService.uploadImage(fundraiserImages, multipartFile);
				PhotoDto photo = new PhotoDto();
				photo.setPhotoUrl(imageName);
				allPhotos.add(photo);
			} catch (IOException e) {
				throw new ApiException("Could not upload files. Try again.", HttpStatus.INTERNAL_SERVER_ERROR, false);
			}
		});

		this.photoService.addAllPhotos(fId, allPhotos);
		return ResponseEntity.ok(new ApiResponse("Images uploaded successfully", true));
	}

	// update photo
	@PatchMapping(value = { "fundraiser_{fId}/photo_{pId}", "fundraiser_{fId}/photo_{pId}/" })
	@PreAuthorize("@fundraiserController.authenticateUser(#fId, authentication.principal.id, hasRole('ADMIN'))")
	public ResponseEntity<?> updateFundraiserPhoto(@Valid @PathVariable Long fId, @PathVariable Long pId,
			@RequestParam("fundraiserPhoto") MultipartFile fundraiserPhoto) {

		if (!this.verifyImage(fundraiserPhoto)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Select valid image");
		}

		PhotoDto photoDto = new PhotoDto();

		try {
			String uploadedPhoto = this.fileService.uploadImage(fundraiserImages, fundraiserPhoto);
			photoDto.setPhotoUrl(uploadedPhoto);
		} catch (IOException e) {
			throw new ApiException("OOPS!! Something went wrong. Could not update image.", HttpStatus.BAD_REQUEST,
					false);
		}

		return ResponseEntity.ok(this.photoService.patchUpdatePhoto(pId, photoDto));

	}

	// delete fundraiser image
	@DeleteMapping(value = { "fundraiser_{fId}/photo_{pId}", "fundraiser_{fId}/photo_{pId}/" })
	@PreAuthorize("@fundraiserController.authenticateUser(#fId, authentication.principal.id, hasRole('ADMIN'))")
	public ResponseEntity<ApiResponse> deleteFundraiserImage(@Valid @PathVariable Long fId, @PathVariable Long pId,
			HttpServletResponse res) {

		this.photoService.deletePhoto(pId);

		return ResponseEntity.ok(new ApiResponse("Image deleted successfully", true));
	}

	// get fundraiser image
	@GetMapping(value = { "fundraiser_{fId}/photo/{imageName}", "fundraiser_{fId}/photo/{imageName}/" })
	public void getFundraiserImage(@Valid @PathVariable Long fId, @PathVariable String imageName,
			HttpServletResponse res) {

		InputStream is;

		try {
			is = this.fileService.getResource(fundraiserImages, imageName);
		} catch (Exception e) {
			throw new ApiException("No such image exists.", HttpStatus.BAD_REQUEST, false);
		}

		res.setContentType(MediaType.IMAGE_JPEG_VALUE);
		try {
			StreamUtils.copy(is, res.getOutputStream());
		} catch (IOException e) {
			throw new ApiException("OOPS!! Something went wrong. Could not get fundraiser image.",
					HttpStatus.INTERNAL_SERVER_ERROR, false);
		}
	}

	// get all fundraiser images
	// <-------------------- -------------------->

	// ------------------ Fundraiser Documents Controllers ------------------
	// add documents
	@PostMapping(value = { "fundraiser_{fId}/add-documents", "fundraiser_{fId}/add-documents/" })
	@PreAuthorize("@fundraiserController.authenticateUser(#fId, authentication.principal.id, hasRole('ADMIN'))")
	public ResponseEntity<?> uploadFundraiserDocuments(@Valid @PathVariable Long fId,
			@RequestParam("documents") MultipartFile[] documents) {

		for (MultipartFile document : documents) {
			if (!this.verifyImage(document)) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Select valid image");
			}
		}

		List<FundraiserDocumentDto> allDocuments = new ArrayList<>();

		Arrays.stream(documents).forEach(multipartFile -> {
			try {
				String documentName = this.fileService.uploadImage(fundraiserDocuments, multipartFile);
				FundraiserDocumentDto document = new FundraiserDocumentDto();
				document.setDocumentUrl(documentName);
				allDocuments.add(document);
			} catch (IOException e) {
				throw new ApiException("Could not upload files. Try again.", HttpStatus.INTERNAL_SERVER_ERROR, false);
			}
		});
		this.documentService.addAllDocuments(fId, allDocuments);
		return ResponseEntity.ok(new ApiResponse("Documents uploaded successfully", true));
	}

	// update fundraiser document
	@PatchMapping(value = { "fundraiser_{fId}/document_{dId}", "fundraiser_{fId}/document_{dId}" })
	@PreAuthorize("@fundraiserController.authenticateUser(#fId, authentication.principal.id, hasRole('ADMIN'))")
	public ResponseEntity<?> updateFundraiserDocument(@Valid @PathVariable Long fId, @PathVariable Long dId,
			@RequestParam("document") MultipartFile document) {

		if (!this.verifyImage(document)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Select valid image");
		}

		FundraiserDocumentDto documentDto = new FundraiserDocumentDto();
		try {
			String uploadedDocument = this.fileService.uploadImage(this.fundraiserDocuments, document);
			documentDto.setDocumentUrl(uploadedDocument);
		} catch (IOException e) {
			throw new ApiException("OOPS!! Something went wrong. Could not update document.", HttpStatus.BAD_REQUEST,
					false);
		}

		return ResponseEntity.ok(this.documentService.patchUpdateDocument(dId, documentDto));
	}

	// delete fundraiser document
	@DeleteMapping(value = { "fundraiser_{fId}/document_{dId}", "fundraiser_{fId}/document_{dId}" })
	@PreAuthorize("@fundraiserController.authenticateUser(#fId, authentication.principal.id, hasRole('ADMIN'))")
	public ResponseEntity<ApiResponse> deleteFundraiserDocument(@Valid @PathVariable Long fId, @PathVariable Long dId,
			HttpServletResponse res) {

		this.documentService.deleteDocument(dId);

		return ResponseEntity.ok(new ApiResponse("Document deleted successfully", true));
	}

	// get fundraiser document
	@GetMapping(value = { "fundraiser_{fId}/document/{documentName}", "fundraiser_{fId}/document_{documentName}" })
	public void getFundraiserDocument(@Valid @PathVariable Long fId, @PathVariable String documentName,
			HttpServletResponse res) {

		InputStream is;

		try {
			is = this.fileService.getResource(fundraiserDocuments, documentName);
		} catch (Exception e) {
			throw new ApiException("No such image exists.", HttpStatus.BAD_REQUEST, false);
		}

		res.setContentType(MediaType.IMAGE_JPEG_VALUE);
		try {
			StreamUtils.copy(is, res.getOutputStream());
		} catch (IOException e) {
			throw new ApiException("OOPS!! Something went wrong. Could not get fundraiser document.",
					HttpStatus.INTERNAL_SERVER_ERROR, false);
		}
	}

	// authenticate user
	public boolean authenticateUser(Long fId, Long cUId, Boolean isAdmin) throws AccessDeniedException {

		UserDto postedBy = this.fundraiserService.getFundraiserById(fId).getPostedBy();
		if (postedBy.getId() == cUId || isAdmin) {
			return true;
		}
		throw new ApiException("You are not authorized to perform this action.", HttpStatus.FORBIDDEN, false);
	}

//	validate and verify image
	private Boolean verifyImage(MultipartFile image) {
		if (image.isEmpty()) {
			throw new ApiException("Kindly select a valid image.", HttpStatus.BAD_REQUEST, false);
		}

		if (!image.getContentType().startsWith("image")) {
			throw new ApiException("Kindly select a valid image.", HttpStatus.BAD_REQUEST, false);
		}
		return true;
	}
}
