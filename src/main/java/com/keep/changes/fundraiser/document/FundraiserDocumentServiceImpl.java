package com.keep.changes.fundraiser.document;

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
public class FundraiserDocumentServiceImpl implements FundraiserDocumentService {

	@Autowired
	private FundraiserDocumentRepository documentRepository;

	@Autowired
	private FundraiserRepository fundraiserRepository;

	@Autowired
	private FileService fileService;

	@Autowired
	private ModelMapper modelMapper;

	@Value("${fundraiser.documents}")
	private String fundraiserDocuments;

	@Override
	public FundraiserDocumentDto addPhoto(Long fId, FundraiserDocumentDto documentDto) {
		Fundraiser fundraiser = this.fundraiserRepository.findById(fId)
				.orElseThrow(() -> new ResourceNotFoundException("Fundraiser", "Id", fId));

		FundraiserDocument document = this.modelMapper.map(documentDto, FundraiserDocument.class);
		document.setFundraiser(fundraiser);
		FundraiserDocument savedDocument = this.documentRepository.save(document);
		return this.modelMapper.map(savedDocument, FundraiserDocumentDto.class);
	}

	@Override
	public List<FundraiserDocumentDto> addAllDocuments(Long fId, List<FundraiserDocumentDto> documentDto) {
		Fundraiser fundraiser = this.fundraiserRepository.findById(fId)
				.orElseThrow(() -> new ResourceNotFoundException("Fundraiser", "Id", fId));

		List<FundraiserDocument> allDocuments = new ArrayList<>();
		for (FundraiserDocumentDto docDto : documentDto) {
			FundraiserDocument document = this.modelMapper.map(docDto, FundraiserDocument.class);
			document.setFundraiser(fundraiser);
			;
			allDocuments.add(document);
		}

		List<FundraiserDocument> savedAll = this.documentRepository.saveAll(allDocuments);

		List<FundraiserDocumentDto> allDtos = new ArrayList<>();
		for (FundraiserDocument document : savedAll) {
			allDtos.add(this.modelMapper.map(document, FundraiserDocumentDto.class));
		}
		return allDtos;
	}

	@Override
	public FundraiserDocumentDto patchUpdateDocument(Long dId, FundraiserDocumentDto documentDto) {

		FundraiserDocument document = this.documentRepository.findById(dId)
				.orElseThrow(() -> new ResourceNotFoundException("Document", "Id", dId));

		try {
			this.fileService.deleteFile(fundraiserDocuments, document.getDocumentUrl());
		} catch (IOException e) {
			throw new ApiException("OOPS!! Something went wrong. Could not update document.",
					HttpStatus.INTERNAL_SERVER_ERROR, false);
		}

		document.setDocumentUrl(documentDto.getDocumentUrl());
		FundraiserDocument savedDocument = this.documentRepository.save(document);
		return this.modelMapper.map(savedDocument, FundraiserDocumentDto.class);
	}

	@Override
	public void deleteDocument(Long dId) {
		FundraiserDocument document = this.documentRepository.findById(dId)
				.orElseThrow(() -> new ResourceNotFoundException("Document", "Id", dId));

		try {
			this.fileService.deleteFile(fundraiserDocuments, document.getDocumentUrl());
		} catch (IOException e) {
			throw new ApiException("OOPS!! Something went wrong. Could not delete document image.",
					HttpStatus.INTERNAL_SERVER_ERROR, false);
		}

		this.documentRepository.delete(document);
	}

	@Override
	public List<FundraiserDocumentDto> getAllDocument() {
		List<FundraiserDocument> all = this.documentRepository.findAll();
		List<FundraiserDocumentDto> allDtos = new ArrayList<>();

		for (FundraiserDocument document : all) {
			allDtos.add(this.modelMapper.map(document, FundraiserDocumentDto.class));
		}
		return allDtos;
	}

	@Override
	public FundraiserDocumentDto getDocumentById(Long dId) {
		FundraiserDocument document = this.documentRepository.findById(dId)
				.orElseThrow(() -> new ResourceNotFoundException("Document", "Id", dId));

		return this.modelMapper.map(document, FundraiserDocumentDto.class);
	}

	@Override
	public FundraiserDocumentDto getDocumentByName(String documentName) {
		FundraiserDocument document = this.documentRepository.findByDocumentUrl(documentName)
				.orElseThrow(() -> new ResourceNotFoundException("Document", "Name", documentName));

		return this.modelMapper.map(document, FundraiserDocumentDto.class);
	}

	@Override
	public List<FundraiserDocumentDto> getDocumentsByFundraiser(Long fId) {
		Fundraiser fundraiser = this.fundraiserRepository.findById(fId)
				.orElseThrow(() -> new ResourceNotFoundException("Fundraiser", "Id", fId));

		List<FundraiserDocument> fundraiserDocuments = this.documentRepository.findByFundraiser(fundraiser);
		List<FundraiserDocumentDto> allDtos = new ArrayList<>();
		for (FundraiserDocument document : fundraiserDocuments) {
			allDtos.add(this.modelMapper.map(document, FundraiserDocumentDto.class));
		}
		return allDtos;
	}

}
