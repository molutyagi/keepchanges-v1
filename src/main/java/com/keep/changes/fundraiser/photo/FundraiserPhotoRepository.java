package com.keep.changes.fundraiser.photo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.keep.changes.fundraiser.Fundraiser;

@Repository
public interface FundraiserPhotoRepository extends JpaRepository<Photo, Long> {

	List<Photo> findByFundraiser(Fundraiser fundraiser);

	Optional<Photo> findByPhotoUrl(String photoUrl);

}
