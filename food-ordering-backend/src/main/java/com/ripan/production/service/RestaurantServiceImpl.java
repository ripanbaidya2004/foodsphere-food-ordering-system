package com.ripan.production.service;

import com.ripan.production.dto.RestaurantDto;
import com.ripan.production.model.Address;
import com.ripan.production.model.Restaurant;
import com.ripan.production.model.User;
import com.ripan.production.repository.AddressRepository;
import com.ripan.production.repository.RestaurantRepository;
import com.ripan.production.repository.UserRepository;
import com.ripan.production.request.CreateRestaurantRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService{

    private final RestaurantRepository restaurantRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    @Override public Restaurant createRestaurant(CreateRestaurantRequest restaurantRequest, User user) {
        validateAddressFields(restaurantRequest.getAddress());

        Address address = addressRepository.save(restaurantRequest.getAddress());
        Restaurant restaurant = new Restaurant();

        restaurant.setName(restaurantRequest.getName());
        restaurant.setDescription(restaurantRequest.getDescription());
        restaurant.setCuisineType(restaurantRequest.getCuisineType());
        restaurant.setOpeningHours(restaurantRequest.getOpeningHours());
        restaurant.setRegistrationDate(LocalDateTime.now());
        restaurant.setAddress(address);

        restaurant.setContactInformation(restaurantRequest.getContactInformation());
        restaurant.setImages(restaurantRequest.getImages());
        restaurant.setOwner(user);

        return restaurantRepository.save(restaurant);
    }

    private void validateAddressFields(Address address) {
        if (address.getCity() == null) throw new IllegalArgumentException("City cannot be null");
    }

    @Override public Restaurant updateRestaurant(Long restaurantId, CreateRestaurantRequest updatedRestaurant) throws Exception {

        Restaurant restaurant = findRestaurantById(restaurantId);

        if(restaurant.getName() != null)    restaurant.setName(updatedRestaurant.getName());
        if(restaurant.getDescription() != null)   restaurant.setDescription(updatedRestaurant.getDescription());
        if(restaurant.getAddress() != null)    restaurant.setAddress(updatedRestaurant.getAddress());
        if(restaurant.getCuisineType() != null)  restaurant.setCuisineType(updatedRestaurant.getCuisineType());
        if(restaurant.getOpeningHours() != null)   restaurant.setOpeningHours(updatedRestaurant.getOpeningHours());

        return restaurantRepository.save(restaurant);
    }

    @Override public void deleteRestaurant(Long restaurantId) throws Exception {
        Restaurant restaurant = findRestaurantById(restaurantId);
        restaurantRepository.delete(restaurant);
    }

    @Override public List<Restaurant> getAllRestaurant() {
        return restaurantRepository.findAll();
    }

    @Override public List<Restaurant> searchRestaurant(String keyword) {
        return restaurantRepository.findBySearchQuery(keyword);
    }

    @Override public Restaurant findRestaurantById(Long restaurantId) throws Exception {

        Optional<Restaurant> selectedRestaurant = restaurantRepository.findById(restaurantId);
        if(selectedRestaurant.isEmpty()) throw new Exception("Restaurant not found with id: " + restaurantId);

        return selectedRestaurant.get();
    }

    @Override public Restaurant findRestaurantByUserId(Long userId) throws Exception {
        Restaurant restaurant = restaurantRepository.findByOwnerId(userId);
        if(restaurant == null) throw new Exception("Restaurant not found with Owner id : " + userId);

        return restaurant;
    }

    @Override public RestaurantDto addToFavourites(Long restaurantId, User user) throws Exception {
        /*
        * checking : if it is present then remove it from the favourites or else add it.
         */
        Restaurant restaurant = findRestaurantById(restaurantId);
        RestaurantDto favoriteRestaurantDto = new RestaurantDto();

        favoriteRestaurantDto.setDescription(restaurant.getDescription());
        favoriteRestaurantDto.setImages(restaurant.getImages());
        favoriteRestaurantDto.setTitle(restaurant.getName());
        favoriteRestaurantDto.setId(restaurantId);

        boolean isFavourite = false ;
        List<RestaurantDto> favourites = user.getFavourites();
        for(RestaurantDto favourite : favourites){
            if(favourite.getId().equals(restaurantId)){
                isFavourite = true;
                break;
            }
        }
        if(isFavourite){
            favourites.removeIf(favourite -> favourite.getId().equals(restaurantId));
        }else {
            favourites.add(favoriteRestaurantDto);
        }
        userRepository.save(user);

        return favoriteRestaurantDto;
    }

    @Override public Restaurant updateRestaurantStatus(Long id) throws Exception {

        Restaurant restaurant = findRestaurantById(id);
        restaurant.setOpen(!restaurant.isOpen());
        return restaurantRepository.save(restaurant);
    }
}
