package com.keep.changes.pan;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.keep.changes.exception.ApiException;
import com.keep.changes.exception.ResourceNotFoundException;
import com.keep.changes.user.User;
import com.keep.changes.user.UserRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PanServiceImpl implements PanService {

	@Autowired
	private PanRepository panRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Override
	public PanDto addPan(PanDto panDto) {

		String loggedInUser = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = this.userRepository.findByEmail(loggedInUser)
				.orElseThrow(() -> new ResourceNotFoundException("User", "Username", loggedInUser));

		Pan pan = this.modelMapper.map(panDto, Pan.class);
		pan.setPanHolder(user);
		Pan savedPan = this.panRepository.save(pan);

		return this.modelMapper.map(savedPan, PanDto.class);
	}

	@Override
	public PanDto putUpdatePan(PanDto pd, Long pId) {

		Pan pan = this.panRepository.findById(pId).orElseThrow(() -> new ResourceNotFoundException("Pan", "Id", pId));

		pan.putUpdatePan(pId, pd.getPanNumber(), pd.getNameOnPan(), pd.getAddress(), pd.getCity(), pd.getState(),
				pd.getCountry(), pd.getPincode());

		Pan updatedPan = this.panRepository.save(pan);

		return this.modelMapper.map(updatedPan, PanDto.class);
	}

	@Override
	public PanDto patchUpdatePan(PanDto partialPanDto, Long pId) {

		Pan pan = this.panRepository.findById(pId).orElseThrow(() -> new ResourceNotFoundException("Pan", "Id", pId));

		Pan partialPan = this.modelMapper.map(partialPanDto, Pan.class);

		Field[] declaredFields = Pan.class.getDeclaredFields();

		for (Field field : declaredFields) {
			field.setAccessible(true);
			try {
				Object value = field.get(partialPan);

				if (value != null) {
					field.set(pan, value);
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new ApiException("error updating pan", HttpStatus.BAD_REQUEST, false);
			}
		}

		Pan updatedPan = this.panRepository.save(pan);
		return this.modelMapper.map(updatedPan, PanDto.class);
	}

	@Override
	public void deletePan(Long pId) {

		this.panRepository.deleteById(pId);
	}

	@Override
	public List<PanDto> getAll() {

		List<Pan> all = this.panRepository.findAll();
		List<PanDto> allDtos = new ArrayList<>();

		for (Pan pan : all) {
			allDtos.add(this.modelMapper.map(pan, PanDto.class));
		}
		return allDtos;
	}

	@Override
	public PanDto getById(Long pId) {

		Pan pan = this.panRepository.findById(pId).orElseThrow(() -> new ResourceNotFoundException("Pan", "Id", pId));

		return this.modelMapper.map(pan, PanDto.class);
	}

	@Override
	public PanDto getByPanHolder(Long uId) {
		User user = this.userRepository.findById(uId)
				.orElseThrow(() -> new ResourceNotFoundException("User", "Id", uId));

		Pan pan = this.panRepository.findByPanHolder(user)
				.orElseThrow(() -> new ResourceNotFoundException("Pan", "User", uId));

		return this.modelMapper.map(pan, PanDto.class);
	}

	@Override
	public PanDto getByPanNumber(String panNumber) {
		Pan pan = this.panRepository.findByPanNumber(panNumber)
				.orElseThrow(() -> new ResourceNotFoundException("Pan", "Number", panNumber));

		return this.modelMapper.map(pan, PanDto.class);
	}

	@Override
	public List<PanDto> getByNameOnPan(String name) {

		List<Pan> pansByName = this.panRepository.findByNameOnPanContaining(name);

		List<PanDto> allDtos = new ArrayList<>();

		for (Pan pan : pansByName) {
			allDtos.add(this.modelMapper.map(pan, PanDto.class));
		}
		return allDtos;
	}

}
