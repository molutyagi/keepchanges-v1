package com.keep.changes.fundraiser.document;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import com.keep.changes.fundraiser.Fundraiser;

@Repository
public interface FundraiserDocumentRepository extends JpaRepository<FundraiserDocument, Long> {

	List<FundraiserDocument> findByFundraiser(Fundraiser fundraiser);

	Optional<FundraiserDocument> findByDocumentUrl(String documentUrl);
}
