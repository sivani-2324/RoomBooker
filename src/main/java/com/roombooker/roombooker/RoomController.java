package com.roombooker.roombooker;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

import jakarta.servlet.http.HttpSession;

@Controller
public class RoomController {

    private final RoomRepository roomRepository;

    public RoomController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @GetMapping("/rooms")
    public String viewRooms(
            Model model,
            HttpSession session) {

        // Check login
        String userName =
                (String) session.getAttribute("userName");

        // If user is NOT logged in
        if (userName == null) {
            return "redirect:/";
        }

        // Get all rooms from database
        List<Room> rooms =
                roomRepository.findAll();

        model.addAttribute(
                "rooms",
                rooms
        );

        return "rooms";
    }
}