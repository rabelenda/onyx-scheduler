package com.onyxscheduler.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping("/onyx")
@Controller
public class UIController {

	@RequestMapping(method = RequestMethod.GET)
	public String getHome() {
		return "redirect:/index.html";
	}
}
