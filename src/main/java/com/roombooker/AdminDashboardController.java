
package com.roombooker.roombooker;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminDashboardController {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public AdminDashboardController(
            RoomRepository roomRepository,
            BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/admin-dashboard")
    public String adminDashboard(
            HttpSession session,
            Model model) {

        String userRole =
                (String) session.getAttribute("userRole");

        if (userRole == null || !userRole.equals("Admin")) {
            return "redirect:/";
        }

        long totalRooms = roomRepository.count();
        long totalBookings = bookingRepository.count();

        long activeBookings = bookingRepository.findAll()
                .stream()
                .filter(b -> "Booked".equalsIgnoreCase(b.getStatus()))
                .count();

        long cancelledBookings = bookingRepository.findAll()
                .stream()
                .filter(b -> "Cancelled".equalsIgnoreCase(b.getStatus()))
                .count();

        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("totalBookings", totalBookings);
        model.addAttribute("activeBookings", activeBookings);
        model.addAttribute("cancelledBookings", cancelledBookings);

        return "admin-dashboard";
    }
}
