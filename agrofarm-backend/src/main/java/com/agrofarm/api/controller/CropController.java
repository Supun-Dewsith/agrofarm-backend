package com.agrofarm.api.controller;


import com.agrofarm.api.dto.crops.CropDetails;
import com.agrofarm.api.dto.crops.FarmIDs;
import com.agrofarm.api.dto.crops.Fields;
import com.agrofarm.api.dto.crops.NewCropRequest;
import com.agrofarm.api.service.impl.CropServiceImpl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.json.JsonMapper;
import com.agrofarm.api.dto.crops.CropUpdateDetails;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/crops")
public class CropController {

    private final CropServiceImpl cropService;
    private final JsonMapper objectMapper;

    public CropController(CropServiceImpl cropService, JsonMapper objectMapper){

        this.cropService = cropService;
        this.objectMapper = objectMapper;
    }


    @GetMapping("/farms")
    public List<FarmIDs> getFarmDetails(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        String role = cropService.findRoleByUsername(currentUsername);

        System.out.println(role);

        if (role.equals("FARMER")|| role.equals("SUPERVISOR")) {

            return cropService.getFarmDetails(currentUsername);
        }
        else if (role.equals("ADMIN")) {
            return cropService.getAdminFarmDetails(currentUsername);
        }
        else {
            return Collections.emptyList();
        }

    }

    @PostMapping("/fields")
    public List<Fields> getFields(@RequestBody Long id){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        return cropService.getFieldDetails(id);

    }





    @PostMapping(value = "/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> insertCropDetails(
            @RequestPart("crop") String cropJson,
            @RequestPart(
                    value = "image",
                    required = false
            ) MultipartFile image
    )throws Exception {

        // Convert JSON String → DTO
        NewCropRequest cropDTO =
                objectMapper.readValue(
                        cropJson,
                        NewCropRequest.class
                );
        if (image != null) {
            System.out.println(
                    "Image name: " +
                            image.getOriginalFilename()
            );

        }

        Long cropId = cropService.insertCropData(cropDTO);
        cropService.insertImage(image,cropId);

        return ResponseEntity.ok("Crop received successfully");


    }


    @PostMapping("/crops")
    public List<CropDetails> getCropDetails(@RequestBody Long farmID){

        return cropService.getCropDetails(farmID);

    }



    @DeleteMapping("/{cropId}")
    public ResponseEntity<?> deleteCrop(@PathVariable Long cropId) {
        cropService.deleteCrop(cropId);
        return ResponseEntity.ok().build();
    }




    @GetMapping("/{cropId}")
    public ResponseEntity<CropUpdateDetails> getCropById(
            @PathVariable Long cropId) {

        CropUpdateDetails crop = cropService.getCropById(cropId);

        return ResponseEntity.ok(crop);
    }

    

    @PutMapping("/update/{cropId}")
    public ResponseEntity<?> updateCrop(
            @PathVariable Long cropId,
            @RequestBody NewCropRequest cropDTO) {

        cropService.updateCrop(cropId, cropDTO);

        return ResponseEntity.ok("Crop updated successfully");
    }

}
