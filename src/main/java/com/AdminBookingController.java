package com.roombooker.roombooker;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminBookingController {

    private final BookingRepository bookingRepository;

    public AdminBookingController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }


    // Admin Bookings Page
    @GetMapping("/admin-bookings")
    public String adminBookings(
            Model model,
            HttpSession session) {

        // Check admin login
        String userRole =
                (String) session.getAttribute("userRole");

        if (userRole == null || !userRole.equals("Admin")) {
            return "redirect:/";
        }


        // Get all bookings
        List<Booking> bookings =
                bookingRepository.findAll();

        model.addAttribute(
                "bookings",
                bookings
        );


        return "admin-bookings";
    }

}
