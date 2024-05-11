package com.keep.changes.fundraiser.document;

import java.util.List;

public interface FundraiserDocumentService {
	

//	add
//	single
	FundraiserDocumentDto addDocument(Long fId, FundraiserDocumentDto documentDto);

//	all at once
	List<FundraiserDocumentDto> addAllDocuments(Long fId, List<FundraiserDocumentDto> documentDto);

//	update
	FundraiserDocumentDto patchUpdateDocument(Long dId, FundraiserDocumentDto documentDto);

//	delete
	void deleteDocument(Long dId);

//	get
//	get all
	List<FundraiserDocumentDto> getAllDocument();

//	by Id
	FundraiserDocumentDto getDocumentById(Long dId);

//	get by name
	FundraiserDocumentDto getDocumentByName(String documentName);

//	get by fundraiser
	List<FundraiserDocumentDto> getDocumentsByFundraiser(Long fId);


}
