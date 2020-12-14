package ar.edu.unq.eperdemic.spring.controllers

import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@CrossOrigin
@ServiceREST
@RequestMapping("/group")
class GroupControllerREST(private val groupName : String) {
    @GetMapping
    fun getGroupId() = groupName
}