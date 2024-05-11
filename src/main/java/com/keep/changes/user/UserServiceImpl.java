package com.keep.changes.user;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.keep.changes.auth.AuthenticationResponse;
import com.keep.changes.config.AppConstants;
import com.keep.changes.exception.ApiException;
import com.keep.changes.exception.ResourceNotFoundException;
import com.keep.changes.file.FileService;
import com.keep.changes.role.Role;
import com.keep.changes.role.RoleRepository;
import com.keep.changes.security.jwt.JwtService;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private FileService fileService;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Value("${user-profile.default}")
	private String DEFAULT_PROFILE_IMAGE;

	@Value("${user-profile.default}")
	private String DEFAULT_COVER_IMAGE;

	@Value("${user-profile.images}")
	private String profileImagePath;

	@Value("${user-cover.images}")
	private String coverImagePath;

//	Create User
	@Override
	@Transactional
	public UserDto createUser(UserDto userDto) {

		userDto.setEmail(userDto.getEmail().toLowerCase());

		User user = this.modelMapper.map(userDto, User.class);

		user.setPassword(this.passwordEncoder.encode(userDto.getPassword()));

//		get and set roles
		Role role = this.roleRepository.findById(AppConstants.NORMAL_USER)
				.orElseThrow(() -> new ResourceNotFoundException("Role", "Id", AppConstants.NORMAL_USER));
		user.getRoles().add(role);

		User savedUser = this.userRepository.save(user);

		return this.modelMapper.map(savedUser, UserDto.class);
	}

//	Put Update User
	@Override
	@Transactional
	public UserDto putUpdateUser(Long uId, UserDto ud) {
		User user = this.userRepository.findById(uId)
				.orElseThrow(() -> new ResourceNotFoundException("User", "Id", uId));

		user.setUpdateUser(uId, ud.getName(), ud.getEmail().toLowerCase(),
				this.passwordEncoder.encode(ud.getPassword()), ud.getPhone(), ud.getDisplayImage(), ud.getCoverImage(),
				ud.getAbout());

		User updatedUser = this.userRepository.save(user);
		return this.modelMapper.map(updatedUser, UserDto.class);
	}

//	Patch Update User
	@Override
	@Transactional
	public UserDto patchUpdateUser(Long uId, UserDto partialUserDto) {

		User user = this.userRepository.findById(uId)
				.orElseThrow(() -> new ResourceNotFoundException("User", "Id", uId));

		User partialUser = this.modelMapper.map(partialUserDto, User.class);

		Field[] declaredFields = User.class.getDeclaredFields();
		for (Field field : declaredFields) {
			field.setAccessible(true);
			try {
				Object value = field.get(partialUser);

				if (value != null) {

					if (field.getName().equals("roles") || field.getName().equals("accounts")
							|| field.getName().equals("fundraisers") || field.getName().equals("donations")) {
						break;
					}

					if (field.getName().equals("password")) {
						value = this.passwordEncoder.encode(value.toString());
					}

					if (field.getName().equals("email")) {
						value = value.toString().toLowerCase();
					}

					if (field.getName().equals("displayImage")) {
						this.hasPreviousProfile(user);
					}

					if (field.getName().equals("coverImage")) {
						this.hasPreviousCover(user);
					}

					field.set(user, value);
				}

			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException("error updating user", e);
			}
		}
		User updatedUser = this.userRepository.save(user);
		return this.modelMapper.map(updatedUser, UserDto.class);
	}

//	Update user email
	public AuthenticationResponse updateUserEmail(Long uId, UserDto userDto) {
		User user = this.userRepository.findById(uId)
				.orElseThrow(() -> new ResourceNotFoundException("User", "Id", uId));

		user.setEmail(userDto.getEmail().toLowerCase());
		this.userRepository.save(user);
		String accessToken = this.jwtService.generateAccessToken(user);
		String refreshToken = this.jwtService.generateRefreshToken(user);

		AuthenticationResponse response = new AuthenticationResponse();
		response.setAccessToken(accessToken);
		response.setRefreshToken(refreshToken);

		return response;
	}

//	Delete User
	@Override
	@Transactional
	public void deleteUser(Long uId) {
		User user = this.userRepository.findById(uId)
				.orElseThrow(() -> new ResourceNotFoundException("User", "Id", uId));

		try {
			this.hasPreviousProfile(user);
			this.hasPreviousCover(user);
			this.userRepository.delete(user);
		} catch (Exception e) {
			throw new ApiException("OOPS!! Something went wrong. Could not delete user.",
					HttpStatus.INTERNAL_SERVER_ERROR, false);
		}
	}

//	Delete profile image
	@Override
	@Transactional
	public boolean deleteProfileImage(long uId) {
		User user = this.userRepository.findById(uId)
				.orElseThrow(() -> new ResourceNotFoundException("User", "Id", uId));

		if (!this.hasPreviousProfile(user)) {
			return false;
		}
		user.setDisplayImage(DEFAULT_COVER_IMAGE);
		this.userRepository.save(user);
		return true;
	}

//	Delete cover image
	@Override
	@Transactional
	public boolean deleteCoverImage(long uId) {
		User user = this.userRepository.findById(uId)
				.orElseThrow(() -> new ResourceNotFoundException("User", "Id", uId));
		System.out.println("user found");
		if (!this.hasPreviousCover(user)) {
			return false;
		}
		user.setCoverImage(DEFAULT_COVER_IMAGE);
		this.userRepository.save(user);
		return true;
	}

//	Get User

//	Get currently logged in user
	public UserDto getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User currentUser = (User) authentication.getPrincipal();

		return this.modelMapper.map(currentUser, UserDto.class);
	}

//	By Id
	@Override
	@Transactional
	public UserDto getUserById(Long uId) {
		User user = this.userRepository.findById(uId)
				.orElseThrow(() -> new ResourceNotFoundException("User", "Id", uId));

		return this.modelMapper.map(user, UserDto.class);
	}

//	Get All users
	@Override
	@Transactional
	public List<UserDto> getAllUsers() {

		List<User> users = this.userRepository.findAll();

		List<UserDto> userDtos = new ArrayList<>();

		for (User user : users) {

			UserDto userDto = this.modelMapper.map(user, UserDto.class);
			userDtos.add(userDto);
		}

		return userDtos;
	}

//	Get User by Email
	@Override
	@Transactional
	public UserDto getUserByEmail(String email) {
		User user = this.userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User", "Email", email));

		return this.modelMapper.map(user, UserDto.class);
	}

//	Get User by Email containing 
	@Override
	@Transactional
	public List<UserDto> getUsersByEmailContaining(String email) {
		List<User> users = this.userRepository.findByEmailContaining(email)
				.orElseThrow(() -> new ResourceNotFoundException("User", "Email", email));

		List<UserDto> userDtos = new ArrayList<>();
		for (User user : users) {
			UserDto userDto = this.modelMapper.map(user, UserDto.class);
			userDtos.add(userDto);
		}
		return userDtos;
	}

//	Get User By Phone
	@Override
	@Transactional
	public UserDto getUserByPhone(String phone) {
		User user = this.userRepository.findByPhone(phone)
				.orElseThrow(() -> new ResourceNotFoundException("User", "Phone", phone));

		return this.modelMapper.map(user, UserDto.class);

	}

//	Get User By Name
	@Override
	@Transactional
	public List<UserDto> getUsersByName(String name) {
		List<User> users = this.userRepository.findByNameContaining(name)
				.orElseThrow(() -> new ResourceNotFoundException("User", "name", name));

		List<UserDto> userDtos = new ArrayList<>();
		for (User user : users) {
			UserDto userDto = this.modelMapper.map(user, UserDto.class);
			userDtos.add(userDto);
		}
		return userDtos;
	}

//	search users
	@Override
	public Set<UserDto> searchUsers(String keyWord) {

		Set<UserDto> userDtos = new HashSet<UserDto>();

		Optional<User> byEmail = this.userRepository.findByEmail(keyWord);
		Optional<User> byPhone = this.userRepository.findByPhone(keyWord);
		List<User> byNameContaining = this.userRepository.findByNameContaining(keyWord).orElse(null);
		List<User> byEmailContaining = this.userRepository.findByEmailContaining(keyWord).orElse(null);

		if (byEmail.isPresent()) {
			userDtos.add(this.modelMapper.map(byEmail, UserDto.class));
		}

		if (byPhone.isPresent()) {
			userDtos.add(this.modelMapper.map(byPhone, UserDto.class));
		}

		if (!byNameContaining.isEmpty()) {
			for (User user : byNameContaining) {
				userDtos.add(this.modelMapper.map(user, UserDto.class));
			}
		}

		if (!byEmailContaining.isEmpty()) {
			for (User user : byEmailContaining) {
				userDtos.add(this.modelMapper.map(user, UserDto.class));
			}
		}

//		if (userDtos.isEmpty()) {
//			throw new ResourceNotFoundException("User", "Keyword", keyWord);
//		}
		return userDtos;
	}

//	check if email already exists
	public boolean emailExists(String email) {
		Optional<User> byEmail = this.userRepository.findByEmail(email);
		if (byEmail.isPresent()) {
			return true;
		}
		return false;
	}

//	check if email already exists
	public boolean phoneExists(String phone) {
		Optional<User> byPhone = this.userRepository.findByPhone(phone);
		if (byPhone.isPresent()) {
			return true;
		}
		return false;
	}

//	delete if previous profile exists
	private boolean hasPreviousProfile(User user) {

		boolean isDeleted = false;

		if (user.getDisplayImage() != null && !user.getDisplayImage().equals("")
				&& !user.getDisplayImage().equals(this.DEFAULT_PROFILE_IMAGE)) {

			try {
				this.fileService.deleteFile(profileImagePath, user.getDisplayImage());
				isDeleted = true;
			} catch (IOException e) {
				throw new ApiException("OOPS!! Something went wrong. Could not update profile image.",
						HttpStatus.BAD_REQUEST, false);
			}

			if (isDeleted == false) {
				throw new ApiException("OOPS!! Something went wrong. Could not update profile image.",
						HttpStatus.BAD_REQUEST, false);
			}
		}
		return isDeleted;
	}

//	delete if previous cover exists
	private boolean hasPreviousCover(User user) {

		boolean isDeleted = false;

		if (user.getCoverImage() != null && !user.getCoverImage().equals("")
				&& !user.getCoverImage().equals(this.DEFAULT_COVER_IMAGE)) {

			try {
				this.fileService.deleteFile(coverImagePath, user.getCoverImage());
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
