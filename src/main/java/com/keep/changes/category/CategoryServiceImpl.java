package com.keep.changes.category;

import java.io.IOException;
import java.lang.reflect.Field;
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

import jakarta.transaction.Transactional;

@Service
public class CategoryServiceImpl implements CategoryService {

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private FileService fileService;

	@Autowired
	private ModelMapper modelMapper;

	@Value("${category-svg-path}")
	private String CATEGORY_SVG_PATH;

	@Value("${category-svg.default}")
	private String DEFAULT_CATEGORY_SVG;

//	add
	@Override
	@Transactional
	public CategoryDto addCategory(CategoryDto categoryDto) {

		Category category = this.modelMapper.map(categoryDto, Category.class);
		Category savedCategory = this.categoryRepository.save(category);

		return this.modelMapper.map(savedCategory, CategoryDto.class);
	}

//	update
//	put update
	@Override
	@Transactional
	public CategoryDto putUpdateCategory(Long cId, CategoryDto cd) {

		Category category = this.categoryRepository.findById(cId)
				.orElseThrow(() -> new ResourceNotFoundException("Category", "Id", cId));

		category.putUpdateCategory(cId, cd.getCategoryName(), cd.getCategoryDescription(), cd.getCategorySvg());

		Category updated = this.categoryRepository.save(category);

		return this.modelMapper.map(updated, CategoryDto.class);
	}

//	patch
	@Override
	@Transactional
	public CategoryDto patchUpdateCategory(Long cId, CategoryDto partialCategoryDto) {

		Category category = this.categoryRepository.findById(cId)
				.orElseThrow(() -> new ResourceNotFoundException("Category", "Id", cId));

		Category partialCategory = this.modelMapper.map(partialCategoryDto, Category.class);

		Field[] declaredFields = Category.class.getDeclaredFields();

		for (Field field : declaredFields) {
			field.setAccessible(true);

			try {

				Object value = field.get(partialCategory);

				if (value != null) {
					if (field.getName().equals("svg")) {
						hasPreviousSvg(category);
					}
					field.set(category, value);
				}

			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new ApiException("Error updating category. Try again!!", HttpStatus.BAD_REQUEST, false);
			}
		}

		Category updated = this.categoryRepository.save(category);

		return this.modelMapper.map(updated, CategoryDto.class);
	}

//	delete
//	delete category
	@Override
	@Transactional
	public void deleteCategory(Long cId) {

		Category category = this.categoryRepository.findById(cId)
				.orElseThrow(() -> new ResourceNotFoundException("Category", "Id", cId));

		this.hasPreviousSvg(category);
		this.categoryRepository.delete(category);

	}

//	delete svg
	@Override
	@Transactional
	public void deleteSvg(Long cId) {
		Category category = this.categoryRepository.findById(cId)
				.orElseThrow(() -> new ResourceNotFoundException("Category", "Id", cId));

		if (category.getCategorySvg() == null || category.getCategorySvg().equals("")
				|| category.getCategorySvg().equals(DEFAULT_CATEGORY_SVG)) {
			throw new ApiException(
					"No SVG exists for the category " + category.getCategoryName() + ".  Kindly upload one.",
					HttpStatus.BAD_REQUEST, false);
		}

		this.hasPreviousSvg(category);
		category.setCategorySvg(DEFAULT_CATEGORY_SVG);
		this.categoryRepository.save(category);
	}

//	get
//	by Id
	@Override
	@Transactional
	public CategoryDto getById(Long cId) {
		Category category = this.categoryRepository.findById(cId)
				.orElseThrow(() -> new ResourceNotFoundException("Category", "Id", cId));

		return this.modelMapper.map(category, CategoryDto.class);
	}

//	get all
	@Override
	@Transactional
	public List<CategoryDto> getAll() {

		List<Category> all = this.categoryRepository.findAll();
		List<CategoryDto> categoryDtos = new ArrayList<>();

		for (Category category : all) {
			CategoryDto categoryDto = this.modelMapper.map(category, CategoryDto.class);
			categoryDtos.add(categoryDto);
		}

		return categoryDtos;
	}

//	delete if previous svg exists
	private boolean hasPreviousSvg(Category category) {

		boolean isDeleted = false;

		if (category.getCategorySvg() != null && category.getCategorySvg().equals("")
				&& !category.getCategorySvg().equals(DEFAULT_CATEGORY_SVG)) {

			try {
				this.fileService.deleteFile(CATEGORY_SVG_PATH, category.getCategorySvg());
				isDeleted = true;
			} catch (IOException e) {
				throw new ApiException("OOPS!! Something went wrong. Could not update SVG.", HttpStatus.BAD_REQUEST,
						false);
			}

			if (isDeleted == false) {
				throw new ApiException("OOPS!! Something went wrong. Could not update SVG.", HttpStatus.BAD_REQUEST,
						false);
			}
		}
		return isDeleted;
	}

}
