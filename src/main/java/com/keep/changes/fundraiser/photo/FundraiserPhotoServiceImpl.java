package com.keep.changes.fundraiser.photo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.keep.changes.exception.ApiException;
import com.keep.changes.exception.ResourceNotFoundException;
import com.keep.changes.file.FileService;
import com.keep.changes.fundraiser.Fundraiser;
import com.keep.changes.fundraiser.FundraiserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class FundraiserPhotoServiceImpl implements FundraiserPhotoService {

	@Autowired
	private FundraiserPhotoRepository photoRepository;

	@Autowired
	private FundraiserRepository fundraiserRepository;

	@Autowired
	private FileService fileService;

	@Autowired
	private ModelMapper modelMapper;

	@Value("${fundraiser.images}")
	private String fundraiserImages;

	@Override
	public PhotoDto addPhoto(PhotoDto photoDto) {
		Photo photo = this.modelMapper.map(photoDto, Photo.class);
		Photo savedPhoto = this.photoRepository.save(photo);
		return this.modelMapper.map(savedPhoto, PhotoDto.class);
	}

	@Override
	public List<PhotoDto> addAllPhotos(Long fId, List<PhotoDto> photoDtos) {

		Fundraiser fundraiser = this.fundraiserRepository.findById(fId)
				.orElseThrow(() -> new ResourceNotFoundException("Fundraiser", "Id", fId));

		List<Photo> allPhotos = new ArrayList<>();
		for (PhotoDto photoDto : photoDtos) {
			Photo photo = this.modelMapper.map(photoDto, Photo.class);
			photo.setFundraiser(fundraiser);
			allPhotos.add(photo);
		}

		List<Photo> savedAll = this.photoRepository.saveAll(allPhotos);

		List<PhotoDto> allDtos = new ArrayList<>();
		for (Photo photo : savedAll) {
			allDtos.add(this.modelMapper.map(photo, PhotoDto.class));
		}
		return allDtos;
	}

	@Override
	public PhotoDto patchUpdatePhoto(Long pId, PhotoDto photoDto) {

		Photo photo = this.photoRepository.findById(pId)
				.orElseThrow(() -> new ResourceNotFoundException("Photo", "Id", pId));

		try {
			this.fileService.deleteFile(fundraiserImages, photo.getPhotoUrl());
		} catch (IOException e) {
			throw new ApiException("OOPS!! Something went wrong. Could not update image.",
					HttpStatus.INTERNAL_SERVER_ERROR, false);
		}
		photo.setPhotoUrl(photoDto.getPhotoUrl());
		Photo updatedPhoto = photoRepository.save(photo);
		return this.modelMapper.map(updatedPhoto, PhotoDto.class);
	}

	@Override
	public void deletePhoto(Long pId) {
		Photo photo = this.photoRepository.findById(pId)
				.orElseThrow(() -> new ResourceNotFoundException("Photo", "Id", pId));

		try {
			this.fileService.deleteFile(fundraiserImages, photo.getPhotoUrl());
			;
		} catch (IOException e) {
			throw new ApiException("OOPS!! Something went wrong. Could not fundraiser image.",
					HttpStatus.INTERNAL_SERVER_ERROR, false);
		}

		this.photoRepository.delete(photo);
	}

	@Override
	public List<PhotoDto> getAllPhotos() {
		List<Photo> all = this.photoRepository.findAll();
		List<PhotoDto> allDtos = new ArrayList<>();

		for (Photo photo : all) {
			allDtos.add(this.modelMapper.map(photo, PhotoDto.class));
		}
		return allDtos;
	}

	@Override
	public PhotoDto getPhotoById(Long pId) {
		Photo photo = this.photoRepository.findById(pId)
				.orElseThrow(() -> new ResourceNotFoundException("Photo", "Id", pId));
		return this.modelMapper.map(photo, PhotoDto.class);
	}

	@Override
	public PhotoDto getPhotoByName(String photoName) {
		Photo photo = this.photoRepository.findByPhotoUrl(photoName)
				.orElseThrow(() -> new ResourceNotFoundException("Photo", "Name", photoName));
		return this.modelMapper.map(photo, PhotoDto.class);
	}

	@Override
	public List<PhotoDto> getPhotosByFundraiser(Long fId) {
		Fundraiser fundraiser = this.fundraiserRepository.findById(fId)
				.orElseThrow(() -> new ResourceNotFoundException("Fundraiser", "Id", fId));
		List<Photo> byFundraiser = this.photoRepository.findByFundraiser(fundraiser);
		List<PhotoDto> allDtos = new ArrayList<>();

		for (Photo photo : byFundraiser) {
			allDtos.add(this.modelMapper.map(photo, PhotoDto.class));
		}
		return allDtos;
	}

}
