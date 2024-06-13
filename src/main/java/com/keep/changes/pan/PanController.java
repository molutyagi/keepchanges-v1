package com.keep.changes.pan;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.keep.changes.payload.response.ApiResponse;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("pans")
public class PanController {

	@Autowired
	private PanService panService;

//	Add
	@PostMapping(value = { "add", "add/" })
	public ResponseEntity<PanDto> addPan(@Valid @RequestBody PanDto panDto) {

		return new ResponseEntity<PanDto>(this.panService.addPan(panDto), HttpStatus.CREATED);
	}

//	Update 
//	Put Update
	@PutMapping(value = { "pan/{pId}", "pan/{pId}/" })
	public ResponseEntity<PanDto> putUpdatePan(@Valid @PathVariable Long pId, @RequestBody PanDto panDto) {
		return ResponseEntity.ok(this.panService.putUpdatePan(panDto, pId));
	}

//	Patch Update
	@PatchMapping(value = { "pan/{pId}", "pan/{pId}/" })
	public ResponseEntity<PanDto> patchUpdatePan(@Valid @PathVariable Long pId, @RequestBody PanDto panDto) {
		return ResponseEntity.ok(this.panService.patchUpdatePan(panDto, pId));
	}

//	Delete
	@DeleteMapping(value = { "pan/{pId}", "pan/{pId}/" })
	public ResponseEntity<ApiResponse> deletePan(@Valid @PathVariable Long pId) {
		this.panService.deletePan(pId);
		return ResponseEntity.ok(new ApiResponse("Pan deleted successfully", true));
	}

//	Get
//	All
	@GetMapping(value = { "", "/", "getall", "getall/" })
	public ResponseEntity<List<PanDto>> getAll() {

		return ResponseEntity.ok(this.panService.getAll());
	}

//	By Id
	@GetMapping(value = { "{pId}", "{pId}/" })
	public ResponseEntity<PanDto> getById(@Valid @PathVariable Long pId) {

		return ResponseEntity.ok(this.panService.getById(pId));
	}

//	By Pan Number
	@GetMapping(value = { "pan/{number}", "pan/{number}/" })
	public ResponseEntity<PanDto> getBypanNumber(@Valid @PathVariable String number) {

		return ResponseEntity.ok(this.panService.getByPanNumber(number));
	}

//	By Holder
	@GetMapping(value = { "user/{uId}", "user/{uId}/" })
	public ResponseEntity<PanDto> getByHolder(@Valid @PathVariable Long uId) {

		return ResponseEntity.ok(this.panService.getByPanHolder(uId));
	}

//	By Name on pan
	@GetMapping(value = { "name/{name}", "name/{name}/" })
	public ResponseEntity<List<PanDto>> getByNameOnPan(@Valid @PathVariable String name) {

		return ResponseEntity.ok(this.panService.getByNameOnPan(name));
	}

}
