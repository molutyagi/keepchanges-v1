package com.keep.changes.fundraiser;

import jakarta.persistence.PostRemove;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.keep.changes.file.FileService;
import com.keep.changes.fundraiser.document.FundraiserDocument;
import com.keep.changes.fundraiser.photo.Photo;

@Component
public class FundraiserEntityListener {

	@Autowired
	private FileService fileService;

	@PostRemove
	public void postRemove(Fundraiser fundraiser) {
		try {
			// Delete display image from directory
			fileService.deleteFile(null, fundraiser.getDisplayPhoto());

			// Delete photos from directory
			for (Photo photo : fundraiser.getPhotos()) {
				fileService.deleteFile(null, photo.getPhotoUrl());
			}

			// Delete documents from directory
			for (FundraiserDocument document : fundraiser.getDocuments()) {
				fileService.deleteFile(null, document.getDocumentUrl());
			}
		} catch (IOException e) {
			// Log error, but do not throw exception to avoid rollback issues
			System.err.println("Failed to delete files for fundraiser with ID: " + fundraiser.getId());
			e.printStackTrace();
		}
	}
}
