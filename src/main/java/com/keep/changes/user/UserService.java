package com.keep.changes.user;

import java.util.List;
import java.util.Set;

import com.keep.changes.auth.AuthenticationResponse;

public interface UserService {

//	Add User
	UserDto createUser(UserDto userDto);

//	Put Update User
	UserDto putUpdateUser(Long uId, UserDto userDto);

//	Patch Update User
	UserDto patchUpdateUser(Long uId, UserDto userDto);

//	Update user email
	AuthenticationResponse updateUserEmail(Long uId, UserDto userDto);

//	Delete User
	void deleteUser(Long uId);

//	delete profile image
	boolean deleteProfileImage(long uId);

//	delete cover image
	boolean deleteCoverImage(long uId);

//	Get Users
	UserDto getUserById(Long uId);

	List<UserDto> getAllUsers();

	UserDto getUserByEmail(String email);

	List<UserDto> getUsersByEmailContaining(String email);

	UserDto getUserByPhone(String phone);

	List<UserDto> getUsersByName(String name);

	Set<UserDto> searchUsers(String keyWord);

}
