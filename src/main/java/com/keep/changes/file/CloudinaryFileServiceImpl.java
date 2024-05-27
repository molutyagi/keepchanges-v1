package com.keep.changes.file;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Primary
public class CloudinaryFileServiceImpl implements FileService {

	@Autowired
	private Cloudinary cloudinary;

	@Autowired
	private ObjectMapper objectMapper;

//	public String upload(MultipartFile image, String folder) throws IOException {
//
//		Map<?, ?> params = ObjectUtils.asMap("folder", folder);
//
//		try {
//			Map<?, ?> imageData = this.cloudinary.uploader().upload(image.getBytes(), params);
//			CloudinaryUploadResponse uploadResponse = objectMapper.convertValue(imageData,
//					CloudinaryUploadResponse.class);
//
//			String publicId = uploadResponse.getPublicId();
//
//			return publicId.substring(publicId.lastIndexOf("/") + 1);
//
//		} catch (IOException e) {
//			throw new IOException();
//		}
//	}

	@Override
	public String uploadImage(String path, MultipartFile file) throws IOException {
		Map<?, ?> params = ObjectUtils.asMap("folder", path);

		try {
			Map<?, ?> imageData = this.cloudinary.uploader().upload(file.getBytes(), params);
			CloudinaryUploadResponse uploadResponse = objectMapper.convertValue(imageData,
					CloudinaryUploadResponse.class);

			String imageUrl = uploadResponse.getUrl();

			System.out.println(uploadResponse.getUrl());

			return imageUrl.substring(imageUrl.indexOf("upload/") + 7);

		} catch (IOException e) {
			throw new IOException();
		}
	}

	@Override
	public InputStream getResource(String path, String fileName) throws FileNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteFile(String path, String fileName) throws IOException {

		String public_id = fileName.substring(fileName.indexOf("/") + 1, fileName.indexOf("."));
		System.out.println("PUBLIC_ID : " + public_id);
		this.cloudinary.uploader().destroy(public_id, ObjectUtils.emptyMap());
	}
}
