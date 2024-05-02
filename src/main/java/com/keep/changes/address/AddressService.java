package com.keep.changes.address;

import java.util.List;

public interface AddressService {

//	Add address
	AddressDto addAddress(AddressDto addressDto);

//	Update
	AddressDto putUpdateAddress(Long aId, AddressDto addressDto);

	AddressDto patchUpdateAddress(Long aId, AddressDto addressDto);

//	delete
	void deleteAddress(Long aId);

//	get
	List<AddressDto> getAll();

	AddressDto getAddressById(Long aId);

}
