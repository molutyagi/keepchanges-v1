package com.keep.changes.fundraiser.photo;

import java.util.List;

public interface FundraiserPhotoService {

//	add
//	single
	PhotoDto addPhoto(PhotoDto photoDto);

//	all at once
	List<PhotoDto> addAllPhotos(Long fId, List<PhotoDto> photoDtos);

//	update
	PhotoDto patchUpdatePhoto(Long pId, PhotoDto photoDto);

//	delete
	void deletePhoto(Long pId);

//	get
//	get all
	List<PhotoDto> getAllPhotos();

//	by Id
	PhotoDto getPhotoById(Long pId);

//	get by name
	PhotoDto getPhotoByName(String photoName);

//	get by fundraiser
	List<PhotoDto> getPhotosByFundraiser(Long fId);

}
