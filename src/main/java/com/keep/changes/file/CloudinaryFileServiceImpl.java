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

	@Override
	public String uploadImage(String path, MultipartFile file) throws IOException {
		Map<?, ?> params = ObjectUtils.asMap("folder", path);

		try {
			Map<?, ?> imageData = this.cloudinary.uploader().upload(file.getBytes(), params);
			CloudinaryUploadResponse uploadResponse = objectMapper.convertValue(imageData,
					CloudinaryUploadResponse.class);
			String imageUrl = uploadResponse.getUrl();
			return imageUrl.substring(imageUrl.indexOf("upload/") + 7);
		} catch (IOException e) {
			throw new IOException();
		}
	}

	@Override
	public String updateImage(String publicId, MultipartFile file) throws IOException {
		Map<?, ?> params = ObjectUtils.asMap("public_id", publicId, "overwrite", true, "invalidate", true);

		try {
			Map<?, ?> imageData = this.cloudinary.uploader().upload(file.getBytes(), params);
			CloudinaryUploadResponse uploadResponse = objectMapper.convertValue(imageData,
					CloudinaryUploadResponse.class);
			String imageUrl = uploadResponse.getUrl();
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
		Map<?, ?> destroy = this.cloudinary.uploader().destroy(public_id, ObjectUtils.emptyMap());
		System.out.println(destroy);
	}
}
