package com.keep.changes.user;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
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
import com.keep.changes.auth.AuthenticationResponse;
import com.keep.changes.exception.ApiException;
import com.keep.changes.exception.ResourceNotFoundException;
import com.keep.changes.file.FileService;
import com.keep.changes.payload.response.ApiResponse;

import io.micrometer.observation.ObservationRegistry;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("users")
public class UserController {

	@Autowired
	private UserService userService;

	@Autowired
	private FileService fileService;

	@Autowired
	private ObservationRegistry observationRegistry;

	@Autowired
	private ObjectMapper objectMapper;

	@Value("${user-profile.images}")
	private String profileImagePath;

	@Value("${user-cover.images}")
	private String coverImagePath;

	@Value("${user-profile.default}")
	private String DEFAULT_PROFILE_IMAGE;

	@Value("${user-profile.default}")
	private String DEFAULT_COVER_IMAGE;

//	POST Mapping / Add user
	@PostMapping("add")
	public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {

		UserDto createdUser = this.userService.createUser(userDto);
		return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
	}

//	Update User
//	PUT Update user
	@PutMapping("user/update_{uId}")
	@PreAuthorize("@userController.authenticatedUser(#uId, authentication.principal.id, hasRole('ADMIN'))")
	public ResponseEntity<UserDto> putUpdateUser(@PathVariable Long uId, @RequestBody UserDto userDto) {

		UserDto updatedUser = this.userService.putUpdateUser(uId, userDto);
		return ResponseEntity.ok(updatedUser);
	}

//	Patch update user
	@PatchMapping("user/update_{uId}")
	@PreAuthorize("@userController.authenticatedUser(#uId, authentication.principal.id, hasRole('ADMIN'))")
	public ResponseEntity<UserDto> patchUpdateUser(@Valid @PathVariable Long uId, @RequestBody UserDto partialUser) {

		UserDto patchedUser = this.userService.patchUpdateUser(uId, partialUser);

		return ResponseEntity.ok(patchedUser);
	}

//	update User Email
	@PatchMapping("user_{uId}/update-email")
	@PreAuthorize("@userController.authenticatedUser(#uId, authentication.principal.id, hasRole('ADMIN'))")
	public ResponseEntity<AuthenticationResponse> updateUserEmail(@Valid @PathVariable Long uId,
			@RequestBody UserDto userDto) {

		AuthenticationResponse responseTokens = this.userService.updateUserEmail(uId, userDto);
		return ResponseEntity.ok(responseTokens);
	}

//	Delete User
	@DeleteMapping("user/delete_{uId}")
	public ResponseEntity<?> deleteUser(@PathVariable Long uId) {

		this.userService.deleteUser(uId);

		return ResponseEntity.ok(new ApiResponse("User Deleted Successfully.", true));
	}

//	GET Mapping / Get Users

//	get currently logged in user
	@GetMapping(value = { "user/me", "user/me/" })
//	@Cacheable
	public ResponseEntity<?> getCurrentUser() {
		System.out.println("aaya ");
		try {
			UserDto currentUser = this.userService.getCurrentUser();
			return ResponseEntity.ok(currentUser);
		} catch (Exception e) {
			throw new ApiException("You are not logged in. Kindly log in", HttpStatus.UNAUTHORIZED, false);
		}
	}

//	Get all
	@Cacheable
	@GetMapping(value = { "getall", "", "/", "getall/" })
	public ResponseEntity<List<UserDto>> getAllUsers() {

		return ResponseEntity.ok(this.userService.getAllUsers());

	}

//	Get by Id
	@GetMapping(value = { "user_{uId}", "user_{uId}/" })
	@Cacheable
	public ResponseEntity<UserDto> getUserById(@PathVariable Long uId) {

		return ResponseEntity.ok(this.userService.getUserById(uId));
	}

//	Get by Email
	@GetMapping(value = { "user/{email}", "user/{email}/" })
	public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {

		return ResponseEntity.ok(this.userService.getUserByEmail(email));
	}

//	Get by Email Containing
	@GetMapping(value = { "email/{email}", "user/{email}/" })
	public ResponseEntity<List<UserDto>> getUsersByEmailContaining(@PathVariable String email) {

		return ResponseEntity.ok(this.userService.getUsersByEmailContaining(email));
	}

//	Get By Phone
	@GetMapping(value = { "user/phone/{phone}", "user/phone/{phone}/" })
	public ResponseEntity<UserDto> getUserByPhone(@PathVariable String phone) {

		return ResponseEntity.ok(this.userService.getUserByPhone(phone));
	}

//	Get By Name
	@GetMapping(value = { "user/name/{name}", "user/name/{name}/" })
	public ResponseEntity<List<UserDto>> getUserByName(@PathVariable String name) {

		return ResponseEntity.ok(this.userService.getUsersByName(name));
	}

//	get user(s)
	@GetMapping(value = { "search/{keyWord}", "user/search/{keyWord}/" })
	public ResponseEntity<Set<UserDto>> searchUsers(@PathVariable String keyWord) {

		return ResponseEntity.ok(this.userService.searchUsers(keyWord));
	}

//	Upload User Profile Image
	@PatchMapping(value = { "user_{uId}/profile-image", "user_{uId}/profile-image/" })
	@PreAuthorize("@userController.authenticatedUser(#uId, authentication.principal.id, hasRole('ADMIN'))")
	public ResponseEntity<?> uploadProfileImage(@Valid @PathVariable Long uId,
			@RequestParam("image") MultipartFile image) throws IOException {

		if (image.isEmpty()) {
			throw new ApiException("Kindly select a valid image", HttpStatus.BAD_REQUEST, false);
		}

		if (!image.getContentType().startsWith("image")) {
			throw new ApiException("Kindly select a valid image", HttpStatus.BAD_REQUEST, false);
		}

		UserDto userDto = new UserDto();
		UserDto updatedUser;
		String fileName = null;

//		save new image in directory
		try {
			fileName = this.fileService.uploadImage(profileImagePath, image);
		} catch (IOException e) {
			throw new ApiException("OOPS!! Something went wrong. Could not update profile image.",
					HttpStatus.BAD_REQUEST, false);
		}

//		save in database
		userDto.setDisplayImage(fileName);
		try {
			updatedUser = this.userService.patchUpdateUser(uId, userDto);
		} catch (Exception e) {
			this.fileService.deleteFile(profileImagePath, fileName);

			throw new ApiException("OOPS!! Something went wrong. Could not update profile image.",
					HttpStatus.INTERNAL_SERVER_ERROR, false);
		}

		return ResponseEntity.ok(updatedUser);
	}

//	Delete Profile Image
	@DeleteMapping(value = { "user_{uId}/profile-image", "user_{uId}/profile-image/" })
	@PreAuthorize("@userController.authenticatedUser(#uId, authentication.principal.id, hasRole('ADMIN'))")
	public ResponseEntity<ApiResponse> deleteUserProfileImage(@PathVariable Long uId) throws IOException {

		if (!this.userService.deleteProfileImage(uId)) {
			ResponseEntity.ok(new ApiResponse("Profile does not exists.", false));
		}

		return ResponseEntity.ok(new ApiResponse("Profile Image Deleted Successfully.", true));
	}

//	Get user profile image
	@GetMapping(value = { "user_{uId}/profile-image/{imageName}", "user_{uId}/profile-image/{imageName}/" })
	public void downloadProfileImage(@Valid @PathVariable Long uId, @PathVariable String imageName,
			HttpServletResponse res) throws IOException {

		InputStream is;

		try {
			is = this.fileService.getResource(profileImagePath, imageName);
		} catch (Exception e) {
			throw new ApiException("OOPS!! Something went wrong. Could not get profile image.", HttpStatus.BAD_REQUEST,
					false);
		}

		res.setContentType(MediaType.IMAGE_JPEG_VALUE);
		StreamUtils.copy(is, res.getOutputStream());
	}

//	Upload User Cover Image
	@PatchMapping(value = { "user_{uId}/cover-image", "user_{uId}/cover-image/" })
	@PreAuthorize("@userController.authenticatedUser(#uId, authentication.principal.id, hasRole('ADMIN'))")
	public ResponseEntity<?> uploadCoverImage(@Valid @PathVariable Long uId,
			@RequestParam("image") MultipartFile image) {

		if (image.isEmpty()) {
			throw new ApiException("OOPS!! Something went wrong. Could not update profile image.",
					HttpStatus.BAD_REQUEST, false);
		}

		if (!image.getContentType().startsWith("image")) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Kindly select a valid image.");
		}

		UserDto userDto = new UserDto();
		UserDto updatedUser;
		String fileName = null;

//		save new image in directory
		try {
			fileName = this.fileService.uploadImage(coverImagePath, image);
		} catch (IOException e) {
			throw new ApiException("OOPS!! Something went wrong. Could not update profile image.",
					HttpStatus.BAD_REQUEST, false);
		}

//		save in database
		userDto.setCoverImage(fileName);
		try {
			updatedUser = this.userService.patchUpdateUser(uId, userDto);
		} catch (Exception e) {
			try {
				this.fileService.deleteFile(coverImagePath, fileName);
			} catch (IOException e1) {
				throw new ApiException("OOPS!! Something went wrong. Could not update profile image.",
						HttpStatus.BAD_REQUEST, false);
			}
			throw new ApiException("OOPS!! Something went wrong. Could not update profile image.",
					HttpStatus.INTERNAL_SERVER_ERROR, false);
		}

		return ResponseEntity.ok(updatedUser);
	}

//	Delete Cover Image
	@DeleteMapping(value = { "user_{uId}/cover-image", "user_{uId}/cover-image/" })
	@PreAuthorize("@userController.authenticatedUser(#uId, authentication.principal.id, hasRole('ADMIN'))")
	public ResponseEntity<ApiResponse> deleteUserCoverImage(@PathVariable Long uId) {

		if (!this.userService.deleteCoverImage(uId)) {
			return ResponseEntity.ok(new ApiResponse("Cover image does not exists.", false));
		}

		return ResponseEntity.ok(new ApiResponse("Cover Image Deleted Successfully.", true));
	}

//	Get user cover image
	@GetMapping(value = { "user_{uId}/cover-image/{imageName}", "user_{uId}/cover-image/{imageName}/" })
	public void downloadcoverImage(@PathVariable Long uId, @PathVariable String imageName, HttpServletResponse res)
			throws IOException {

		InputStream is;

		try {
			is = this.fileService.getResource(coverImagePath, imageName);
		} catch (Exception e) {
			throw new ApiException("OOPS!! Something went wrong. Could not get cover image.", HttpStatus.BAD_REQUEST,
					false);
		}

		res.setContentType(MediaType.IMAGE_JPEG_VALUE);
		StreamUtils.copy(is, res.getOutputStream());
	}

//	Update File And User Details in a single Request
	@PatchMapping(value = { "user/update-profile_{uId}", "user/update-profile_{uId}/" })
	@PreAuthorize("@userController.authenticatedUser(#uId, authentication.principal.id, hasRole('ADMIN'))")
	public ResponseEntity<?> updateUserAndImage(@Valid @PathVariable Long uId,
			@RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
			@RequestParam(value = "coverImage", required = false) MultipartFile coverImage,
			@RequestParam(value = "userData", required = false) String userData) {

//		UserDto userDto = this.userService.getUserById(uId);
		System.out.println("here");
		UserDto userDto = new UserDto();
		UserDto updatedUser;
		String profileImageName = null;
		String coverImageName = null;

		if (userData != null) {
			try {
				userDto = this.objectMapper.readValue(userData, UserDto.class);
//				Set<ConstraintViolation<UserDto>> violations = validator.validate(userDto);
//				if (!violations.isEmpty()) {
//					throw new ConstraintViolationException(violations);
//				}
			} catch (JsonProcessingException e) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Request!");
			}
		}

		if (profileImage != null) {
			if (!profileImage.getContentType().startsWith("image")) {
				throw new ApiException("OOPS!! Something went wrong. Could not update profile image.",
						HttpStatus.BAD_REQUEST, false);
			}
			try {
				profileImageName = this.fileService.uploadImage(profileImagePath, profileImage);
			} catch (IOException e) {
				throw new ApiException("OOPS!! Something went wrong. Could not update profile.",
						HttpStatus.INTERNAL_SERVER_ERROR, false);
			}
			userDto.setDisplayImage(profileImageName);
		}

		if (coverImage != null) {
			if (!coverImage.getContentType().startsWith("image")) {
				throw new ApiException("OOPS!! Something went wrong. Could not update profile image.",
						HttpStatus.BAD_REQUEST, false);
			}

			try {
				coverImageName = this.fileService.uploadImage(coverImagePath, coverImage);
			} catch (IOException e) {
				throw new ApiException("OOPS!! Something went wrong. Could not update profile image.",
						HttpStatus.INTERNAL_SERVER_ERROR, false);
			}
			userDto.setCoverImage(coverImageName);
		}

		try {
			updatedUser = this.userService.patchUpdateUser(uId, userDto);
		} catch (Exception e) {
			try {
				this.fileService.deleteFile(profileImagePath, profileImageName);
			} catch (IOException e1) {
				throw new ApiException("OOPS!! Something went wrong. Could not update profile.",
						HttpStatus.INTERNAL_SERVER_ERROR, false);
			}
			try {
				this.fileService.deleteFile(coverImagePath, coverImageName);
			} catch (IOException e1) {
				throw new ApiException("OOPS!! Something went wrong. Could not update profile.",
						HttpStatus.INTERNAL_SERVER_ERROR, false);
			}
			throw new ResourceNotFoundException("User", "Id", uId);
		}

		return ResponseEntity.ok(updatedUser);
	}

//	check if correct user is asking to change resources
	public boolean authenticatedUser(long uId, long cUId, boolean isAdmin) throws AccessDeniedException {
		if (uId == cUId || isAdmin) {
			return true;
		}
		throw new ApiException("You are not authorized to perform this action.", HttpStatus.FORBIDDEN, false);
	}

////	delete if previous profile exists
//	private boolean hasPreviousProfile(UserDto userDto) {
//		if (userDto.getDisplayImage() != null && !userDto.getDisplayImage().equals("")
//				&& !userDto.getDisplayImage().equals(this.DEFAULT_PROFILE_IMAGE)) {
//
//			boolean isDeleted;
//
//			try {
//				isDeleted = this.fileService.deleteFile(profileImagePath, userDto.getDisplayImage());
//			} catch (IOException e) {
//				throw new ApiException("OOPS!! Something went wrong. Could not update profile image.",
//						HttpStatus.BAD_REQUEST, false);
//			}
//
//			if (isDeleted == false) {
//				throw new ApiException("OOPS!! Something went wrong. Could not update profile image.",
//						HttpStatus.BAD_REQUEST, false);
//			}
//		}
//		return false;
//	}
//
////	delete if previous cover exists
//	private boolean hasPreviousCover(UserDto userDto) {
//
//		if (userDto.getCoverImage() != null && !userDto.getCoverImage().equals("")
//				&& !userDto.getCoverImage().equals(this.DEFAULT_COVER_IMAGE)) {
//
//			boolean isDeleted;
//
//			try {
//				isDeleted = this.fileService.deleteFile(profileImagePath, userDto.getDisplayImage());
//			} catch (IOException e) {
//				throw new ApiException("OOPS!! Something went wrong. Could not update cover image.",
//						HttpStatus.BAD_REQUEST, false);
//			}
//
//			if (isDeleted == false) {
//				throw new ApiException("OOPS!! Something went wrong. Could not update cover image.",
//						HttpStatus.BAD_REQUEST, false);
//			}
//		}
//		return false;
//	}

}
