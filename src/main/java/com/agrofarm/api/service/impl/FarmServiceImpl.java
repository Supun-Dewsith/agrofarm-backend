package com.agrofarm.api.service.impl;


import com.agrofarm.api.dto.FarmRequest;
import com.agrofarm.api.dto.FarmResponse;

import com.agrofarm.api.entity.Farm;
import com.agrofarm.api.entity.UserEntity;

import com.agrofarm.api.repository.FarmRepository;
import com.agrofarm.api.repository.UserRepository;

import com.agrofarm.api.service.FarmService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;


import java.util.List;



@Service
@RequiredArgsConstructor
public class FarmServiceImpl implements FarmService {



    private final FarmRepository farmRepository;

    private final UserRepository userRepository;






    /*
    ======================================
       CURRENT LOGIN USER
    ======================================
    */

    private UserEntity getCurrentUser(){


        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();



        String username =
                authentication.getName();




        return userRepository
                .findByUsername(username)

                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found : "
                                        + username
                        )
                );

    }







    /*
    ======================================
       CREATE FARM
       AUTO OWNER ID
    ======================================
    */

    @Override
    public FarmResponse createFarm(
            FarmRequest request
    ){


        UserEntity user =
                getCurrentUser();




        Farm farm = Farm.builder()

                .ownerId(
                        user.getId()
                )

                .farmName(
                        request.getFarmName()
                )

                .farmAddress(
                        request.getFarmAddress()
                )

                .latitude(
                        request.getLatitude()
                )

                .longtitude(
                        request.getLongtitude()
                )

                .boundary(
                        request.getBoundary()
                )

                .build();




        Farm saved =
                farmRepository.save(farm);




        return mapToResponse(saved);

    }








    /*
    ======================================
       GET USER FARMS
    ======================================
    */

    @Override
    public List<FarmResponse> getAllFarms(){


        UserEntity user =
                getCurrentUser();



        return farmRepository
                .findByOwnerId(user.getId())

                .stream()

                .map(this::mapToResponse)

                .toList();

    }








    /*
    ======================================
       GET FARM BY ID
    ======================================
    */

    @Override
    public FarmResponse getFarmById(
            Long id
    ){


        Farm farm =
                farmRepository.findById(id)

                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Farm not found : "
                                                + id
                                )
                        );



        checkOwner(farm);



        return mapToResponse(farm);

    }









    /*
    ======================================
       UPDATE FARM
    ======================================
    */

    @Override
    public FarmResponse updateFarm(
            Long id,
            FarmRequest request
    ){



        Farm farm =
                farmRepository.findById(id)

                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Farm not found : "
                                                + id
                                )
                        );



        checkOwner(farm);




        farm.setFarmName(
                request.getFarmName()
        );


        farm.setFarmAddress(
                request.getFarmAddress()
        );


        farm.setLatitude(
                request.getLatitude()
        );


        farm.setLongtitude(
                request.getLongtitude()
        );


        farm.setBoundary(
                request.getBoundary()
        );




        Farm updated =
                farmRepository.save(farm);



        return mapToResponse(updated);

    }








    /*
    ======================================
       DELETE FARM
    ======================================
    */

    @Override
    public void deleteFarm(
            Long id
    ){



        Farm farm =
                farmRepository.findById(id)

                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Farm not found : "
                                                + id
                                )
                        );



        checkOwner(farm);



        farmRepository.delete(farm);

    }








    /*
    ======================================
       OWNER CHECK
    ======================================
    */

    private void checkOwner(
            Farm farm
    ){



        UserEntity user =
                getCurrentUser();




        if(
                !farm.getOwnerId()
                        .equals(user.getId())
        ){

            throw new RuntimeException(
                    "You don't have permission"
            );

        }


    }


    private FarmResponse mapToResponse(
            Farm farm
    ){


        return FarmResponse.builder()

                .id(
                        farm.getId()
                )

                .ownerId(
                        farm.getOwnerId()
                )

                .farmName(
                        farm.getFarmName()
                )

                .farmAddress(
                        farm.getFarmAddress()
                )

                .latitude(
                        farm.getLatitude()
                )

                .longtitude(
                        farm.getLongtitude()
                )

                .boundary(
                        farm.getBoundary()
                )

                .build();

    }


}
