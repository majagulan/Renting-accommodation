package com.project.service;

import java.util.List;

import com.project.model.Apartment;
import com.project.model.Reservation;
import com.project.model.User;

public interface ReservationService {

	List<Reservation> findAll();
	List<Reservation> findByApartmentId(Long id);
	Reservation findOne(Long id);
	Reservation save(Reservation reservation);
	Reservation delete(Long id);
	List<Reservation> findUserReservations(String email);
	boolean isAvailable(Apartment apartment, String startDate, String endDate);
	boolean isAvailableForUpdate(Reservation reservation);
	boolean checkDates(String startDate, String endDate);
	Reservation findUserReservationByApartmentId(User user, Long apartmentId);
}
