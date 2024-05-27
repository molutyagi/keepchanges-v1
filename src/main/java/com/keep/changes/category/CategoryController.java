package com.keep.changes.category;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.keep.changes.exception.ApiException;
import com.keep.changes.file.FileService;
import com.keep.changes.payload.response.ApiResponse;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/categories")
public class CategoryController {

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private FileService fileService;

	@Value("${category-svg-path}")
	private String CATEGORY_SVG_PATH;

	@Value("${category-svg.default}")
	private String DEFAULT_CATEGORY_SVG;

//	create complete category in one go
	@PostMapping(value = { "add/", "add" })
	@PreAuthorize("@categoryController.authenticatedUser(hasRole('ADMIN'))")
	public ResponseEntity<?> createCategory(
			@Valid @RequestParam(value = "categorySvg", required = true) MultipartFile categorySvg,
			@RequestParam(value = "categoryName", required = true) String categoryName,
			@RequestParam(value = "categoryDescription", required = true) String categoryDescription) {

//		create a new category dto instance
		CategoryDto categoryDto = new CategoryDto();

//		check if svg is valid
		String imageName;

		try {
			imageName = this.fileService.uploadImage(CATEGORY_SVG_PATH, categorySvg);
		} catch (IOException e) {
			e.printStackTrace();
			throw new ApiException("Invalid svg. Try again.", HttpStatus.BAD_REQUEST, false);
		}

		categoryDto.setCategoryName(categoryName);
		categoryDto.setCategoryDescription(categoryDescription);
		categoryDto.setCategorySvg(imageName);

//		create category
		return ResponseEntity.ok(this.categoryService.addCategory(categoryDto));
	}

//	@PostMapping("add")
//	public ResponseEntity<CategoryDto> addCategory(@Valid @RequestBody CategoryDto categoryDto) {
//		CategoryDto category = this.categoryService.addCategory(categoryDto);
//		return new ResponseEntity<>(category, HttpStatus.CREATED);
//	}

//	patch complete category
	@PatchMapping(value = { "category_{cId}", "category_{cId}/" })
	@PreAuthorize("@categoryController.authenticatedUser(hasRole('ADMIN'))")
	public ResponseEntity<?> updateCategoryAndSvg(@Valid @PathVariable Long cId,
			@RequestParam(value = "categorySvg", required = false) MultipartFile categorySvg,
			@RequestParam(value = "categoryName", required = false) String categoryName,
			@RequestParam(value = "categoryDescription", required = false) String categoryDescription) {

//		create a new category dto instance
		CategoryDto categoryDto = new CategoryDto();

//		check if category data is not null and is valid
		if (categoryName != null) {
			categoryDto.setCategoryName(categoryName);
		}

		if (categoryDescription != null) {
			categoryDto.setCategoryDescription(categoryDescription);
		}

//		check if svg is not null and is valid
		if (categorySvg != null) {
			try {
				String imageName = this.fileService.uploadImage(CATEGORY_SVG_PATH, categorySvg);
				categoryDto.setCategorySvg(imageName);

			} catch (IOException e) {
				throw new ApiException("Invalid svg. Try again.", HttpStatus.BAD_REQUEST, false);
			}
		}

//		update category
		return ResponseEntity.ok(this.categoryService.patchUpdateCategory(cId, categoryDto));
	}

//	delete
	@DeleteMapping(value = { "category_{cId}", "category_{cId}/" })
	@PreAuthorize("@categoryController.authenticatedUser(hasRole('ADMIN'))")
	public ResponseEntity<ApiResponse> deleteCategory(@Valid @PathVariable Long cId) {

		this.categoryService.deleteCategory(cId);

		return ResponseEntity.ok(new ApiResponse("Category Deleted Successfully!!", true));
	}

//	get
//	by Id
	@GetMapping(value = { "category_{cId}", "category_{cId}/" })
	public ResponseEntity<CategoryDto> getById(@Valid @PathVariable Long cId) {

		return ResponseEntity.ok(this.categoryService.getById(cId));
	}

//	get all
	@GetMapping(value = { "getall", "getall/", "", "/" })
	public ResponseEntity<List<CategoryDto>> getAllAccounts() {

		return ResponseEntity.ok(this.categoryService.getAll());
	}

////	add svg
//	@PatchMapping(value = { "category_{cId}/svg", "category_{cId}/svg/" })
////	@PreAuthorize("@categoryController.authenticatedUser(hasRole('ADMIN'))")
//	public ResponseEntity<?> addCategorySvg(@Valid @PathVariable Long cId, @RequestParam("categorySvg") MultipartFile image) {
//
//		CategoryDto categoryDto = this.categoryService.getById(cId);
//
//		if (categoryDto.getCategorySvg() != null && !categoryDto.getCategorySvg().equals("")
//				&& !categoryDto.getCategorySvg().equals(DEFAULT_CATEGORY_SVG)) {
//			boolean isDeleted;
//			try {
//				this.fileService.deleteFile(CATEGORY_SVG_PATH, categoryDto.getCategorySvg());
//				isDeleted = true;
//			} catch (IOException e) {
//				return ResponseEntity.badRequest()
//						.body(new ApiResponse("OOPS!! Something went wrong. Could not update svg.", false));
//			}
//			if (isDeleted == false) {
//				return ResponseEntity.badRequest()
//						.body(new ApiResponse("OOPS!! Something went wrong. Could not update svg.", false));
//			}
//		}
//
//		String fileName = null;
//
//		try {
//			fileName = this.fileService.uploadImage(CATEGORY_SVG_PATH, image);
//		} catch (IOException e) {
//			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//					.body(" OOPS!! Something went wrong. Could not update category svg.");
//		}
//
//		categoryDto.setCategorySvg(fileName);
//
//		return ResponseEntity.ok(this.categoryService.putUpdateCategory(cId, categoryDto));
//	}

	@DeleteMapping(value = { "category_{cId}/svg", "category_{cId}/svg/" })
	@PreAuthorize("@categoryController.authenticatedUser(hasRole('ADMIN'))")
	public ResponseEntity<?> deleteSvg(@Valid @PathVariable Long cId) {

		this.categoryService.deleteSvg(cId);

		return ResponseEntity.ok(new ApiResponse("Category SVG Deleted Successfully.", true));
	}

	@GetMapping(value = { "category_{cId}/svg", "category_{cId}/svg/" })
	public void getSvg(@Valid @PathVariable Long cId, HttpServletResponse res) throws IOException {

		CategoryDto category = this.categoryService.getById(cId);

		InputStream is = this.fileService.getResource(CATEGORY_SVG_PATH, category.getCategorySvg());
		res.setContentType(MediaType.IMAGE_JPEG_VALUE);
		StreamUtils.copy(is, res.getOutputStream());

	}

//	check if correct user is asking to change resources
	public boolean authenticatedUser(boolean isAdmin) throws AccessDeniedException {
		if (isAdmin) {
			return true;
		}
		throw new ApiException("You are not authorized to perform this action.", HttpStatus.FORBIDDEN, false);
	}
}
