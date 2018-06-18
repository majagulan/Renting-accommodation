package com.project.Rentingaccommodation.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.Rentingaccommodation.model.Apartment;
import com.project.Rentingaccommodation.model.Reservation;
import com.project.Rentingaccommodation.repository.ReservationRepository;
import com.project.Rentingaccommodation.service.ReservationService;

@Transactional
@Service
public class JpaReservationService implements ReservationService {

	@Autowired
	private ReservationRepository repository;
	
	@Override
	public List<Reservation> findAll() {
		return repository.findAll();
	}

	@Override
	public List<Reservation> findByApartmentId(Long id) {
		List<Reservation> apartmentReservations = new ArrayList<Reservation>();
		for (Reservation reservation : findAll()) {
			if (reservation.getApartment().getId() == id) {
				apartmentReservations.add(reservation);
			}
		}
		return apartmentReservations;
	}
	
	@Override
	public Reservation findOne(Long id) {
		for (Reservation reservation : findAll()) {
			if (reservation.getId() == id) {
				return reservation;
			}
		}
		return null;
	}

	@Override
	public void save(Reservation reservation) {
		repository.save(reservation);
	}

	@Override
	public Reservation delete(Long id) {
		Reservation reservation = findOne(id);
		if (reservation != null) {
			repository.delete(reservation);
			return reservation;
		}
		return null;
	}
	
	@Override
	public List<Reservation> findUserReservations(String email) {
		List<Reservation> userReservations = new ArrayList<Reservation>();
		for (Reservation reservation : findAll()) {
			if (reservation.getUser().getEmail().equals(email)) {
				userReservations.add(reservation);
			}
		}
		return userReservations;
	}

	@Override
	public boolean isAvailable(Apartment apartment, String startDate, String endDate) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
		List<Reservation> activeReservations = findByApartmentId(apartment.getId());
		System.out.println(activeReservations);
		for (Reservation reservation : activeReservations) {
			if (reservation.getApartment().getId() == apartment.getId()) {
				// Check in-between dates.
				try {
					Date reservationStartDate = dateFormatter.parse(reservation.getStartDate());
					Date reservationEndDate = dateFormatter.parse(reservation.getEndDate());
					Date start = dateFormatter.parse(startDate);
					Date end = dateFormatter.parse(endDate);
					if (start.compareTo(reservationStartDate) >= 0 && end.compareTo(reservationEndDate) <= 0 ||
						start.compareTo(reservationStartDate) < 0 && end.compareTo(reservationEndDate) <= 0 && end.compareTo(reservationStartDate) >= 0 ||
						start.compareTo(reservationStartDate) >= 0 && start.compareTo(reservationEndDate) <= 0 && end.compareTo(reservationEndDate) > 0 && end.compareTo(start) > 0 ||
						start.compareTo(reservationStartDate) < 0 && end.compareTo(reservationEndDate) > 0) {
						return false;
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	@Override
	public boolean checkDates(String startDate, String endDate) {
		SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
		try {
			Date start = dateFormatter.parse(startDate);
			Date end = dateFormatter.parse(endDate);
			System.out.println(start);
			System.out.println(end);
			return start.compareTo(end) >= 0 ? false : true;
		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		}
	}
}
