package com.roombooker.roombooker;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class BookingController {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    public BookingController(
            RoomRepository roomRepository,
            BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/book")
    public String bookingPage(Model model, HttpSession session) {

        String userName = (String) session.getAttribute("userName");

        if (userName == null) {
            return "redirect:/";
        }

        model.addAttribute("rooms", roomRepository.findAll());
        model.addAttribute("today", LocalDate.now().toString());

        return "book";
    }

    @PostMapping("/book")
    public String bookRoom(
            @RequestParam Long roomId,
            @RequestParam String bookingDate,
            @RequestParam(required = false, defaultValue = "false")
                    boolean fullDay,
            @RequestParam(required = false, defaultValue = "")
                    String startTime,
            @RequestParam(required = false, defaultValue = "")
                    String endTime,
            Model model,
            HttpSession session) {

        String userName = (String) session.getAttribute("userName");

        if (userName == null) {
            return "redirect:/";
        }

        try {
            LocalDate date = LocalDate.parse(bookingDate);
            LocalDate today = LocalDate.now();

            if (date.isBefore(today)) {
                return showError(model,
                        "Past dates cannot be booked.");
            }

            if (!fullDay) {
                if (startTime.isBlank() || endTime.isBlank()) {
                    return showError(model,
                            "Please select both start time and end time.");
                }

                LocalTime start = LocalTime.parse(startTime, TIME_FORMAT);
                LocalTime end = LocalTime.parse(endTime, TIME_FORMAT);
                LocalTime now = LocalTime.now();

                if (!end.isAfter(start)) {
                    return showError(model,
                            "End time must be after start time.");
                }

                if (date.equals(today) && !start.isAfter(now)) {
                    return showError(model,
                            "Please select a future start time.");
                }
            }

            boolean conflict = bookingRepository.hasOverlappingBooking(
                    roomId,
                    bookingDate,
                    fullDay ? "00:00" : startTime,
                    fullDay ? "23:59" : endTime,
                    fullDay
            );

            if (conflict) {
                return showError(model,
                        "This room is already booked during that time. Please choose another time.");
            }

            Booking booking = new Booking();
            booking.setRoomId(roomId);
            booking.setUserName(userName);
            booking.setBookingDatetime(bookingDate);
            booking.setFullDay(fullDay);

            if (fullDay) {
                booking.setStartTime("00:00");
                booking.setEndTime("23:59");
                booking.setSlot("Full Day");
            } else {
                booking.setStartTime(startTime);
                booking.setEndTime(endTime);
                booking.setSlot(startTime + " - " + endTime);
            }

            booking.setStatus("Booked");
            bookingRepository.save(booking);

            return "redirect:/my-bookings";

        } catch (DateTimeParseException e) {
            return showError(model,
                    "Invalid date or time. Please select again.");
        }
    }

    private String showError(Model model, String message) {
        model.addAttribute("error", message);
        model.addAttribute("rooms", roomRepository.findAll());
        model.addAttribute("today", LocalDate.now().toString());
        return "book";
    }

    @GetMapping("/my-bookings")
    public String myBookings(Model model, HttpSession session) {

        String userName = (String) session.getAttribute("userName");

        if (userName == null) {
            return "redirect:/";
        }

        model.addAttribute(
                "bookings",
                bookingRepository.findByUserName(userName)
        );

        return "my-bookings";
    }

    @GetMapping("/cancel-booking")
    public String cancelBooking(
            @RequestParam Long bookingId,
            HttpSession session) {

        String userName = (String) session.getAttribute("userName");

        if (userName == null) {
            return "redirect:/";
        }

        Booking booking = bookingRepository.findById(bookingId)
                .orElse(null);

        if (booking != null
                && userName.equals(booking.getUserName())) {
            booking.setStatus("Cancelled");
            bookingRepository.save(booking);
        }

        return "redirect:/my-bookings";
    }
}