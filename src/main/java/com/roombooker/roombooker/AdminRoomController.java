package com.roombooker.roombooker;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminRoomController {

    private final RoomRepository roomRepository;

    public AdminRoomController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    // =========================
    // MANAGE ROOMS
    // =========================
    @GetMapping("/admin-rooms")
    public String adminRooms(
            Model model,
            HttpSession session) {

        String userRole =
                (String) session.getAttribute("userRole");

        // Not logged in OR not Admin
        if (userRole == null || !userRole.equals("Admin")) {
            return "redirect:/";
        }

        List<Room> rooms =
                roomRepository.findAll();

        model.addAttribute("rooms", rooms);

        return "admin-rooms";
    }


    // =========================
    // ADD ROOM PAGE
    // =========================
    @GetMapping("/admin-add-room")
    public String addRoomPage(
            HttpSession session) {

        String userRole =
                (String) session.getAttribute("userRole");

        // Not logged in OR not Admin
        if (userRole == null || !userRole.equals("Admin")) {
            return "redirect:/";
        }

        return "admin-add-room";
    }


    // =========================
    // ADD ROOM
    // =========================
    @PostMapping("/admin-add-room")
    public String addRoom(
            @RequestParam Long roomId,
            @RequestParam String roomName,
            @RequestParam Integer capacity,
            @RequestParam String amenities,
            @RequestParam String status,
            HttpSession session,
            Model model) {

        String userRole =
                (String) session.getAttribute("userRole");

        // Not logged in OR not Admin
        if (userRole == null || !userRole.equals("Admin")) {
            return "redirect:/";
        }

        // Check duplicate Room ID
        if (roomRepository.existsById(roomId)) {

            model.addAttribute(
                    "error",
                    "Room ID already exists."
            );

            return "admin-add-room";
        }

        Room room = new Room();

        room.setRoomId(roomId);
        room.setRoomName(roomName);
        room.setCapacity(capacity);
        room.setAmenities(amenities);
        room.setStatus(status);

        roomRepository.save(room);

        return "redirect:/admin-rooms";
    }


    // =========================
    // EDIT ROOM PAGE
    // =========================
    @GetMapping("/admin-edit-room")
    public String editRoomPage(
            @RequestParam Long roomId,
            Model model,
            HttpSession session) {

        String userRole =
                (String) session.getAttribute("userRole");

        // Not logged in OR not Admin
        if (userRole == null || !userRole.equals("Admin")) {
            return "redirect:/";
        }

        Room room =
                roomRepository.findById(roomId)
                .orElse(null);

        if (room == null) {
            return "redirect:/admin-rooms";
        }

        model.addAttribute("room", room);

        return "admin-edit-room";
    }


    // =========================
    // UPDATE ROOM
    // =========================
    @PostMapping("/admin-edit-room")
    public String updateRoom(
            @RequestParam Long roomId,
            @RequestParam String roomName,
            @RequestParam Integer capacity,
            @RequestParam String amenities,
            @RequestParam String status,
            HttpSession session) {

        String userRole =
                (String) session.getAttribute("userRole");

        // Not logged in OR not Admin
        if (userRole == null || !userRole.equals("Admin")) {
            return "redirect:/";
        }

        Room room =
                roomRepository.findById(roomId)
                .orElse(null);

        if (room != null) {

            room.setRoomName(roomName);
            room.setCapacity(capacity);
            room.setAmenities(amenities);
            room.setStatus(status);

            roomRepository.save(room);
        }

        return "redirect:/admin-rooms";
    }


    // =========================
    // DELETE ROOM
    // =========================
    @GetMapping("/admin-delete-room")
    public String deleteRoom(
            @RequestParam Long roomId,
            HttpSession session) {

        String userRole =
                (String) session.getAttribute("userRole");

        // Not logged in OR not Admin
        if (userRole == null || !userRole.equals("Admin")) {
            return "redirect:/";
        }

        if (roomRepository.existsById(roomId)) {

            roomRepository.deleteById(roomId);
        }

        return "redirect:/admin-rooms";
    }

}