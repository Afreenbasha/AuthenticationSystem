package com.springboot.fullstackproject.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class EmailService {

	private final JavaMailSender mailSender;
	private final SpringTemplateEngine templateEngine;

	@Value("${spring.mail.properties.mail.smtp.from}")
	private String fromEmail;

	public void sendWelcomeEmail(String toEmail, String name) {
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setFrom(fromEmail);
		mailMessage.setTo(toEmail);
		mailMessage.setSubject("Welcome to Our Platform");
		mailMessage.setText("Hello " + name + ",\n\nThanks for registering with us !\n\n Regards,\n Afreen Team");
		mailSender.send(mailMessage);
	}

//	public void sendResetOtpEmail(String toEmail, String otp) {
//		SimpleMailMessage mailMessage = new SimpleMailMessage();
//		mailMessage.setFrom(fromEmail); // Injected from application.properties
//		mailMessage.setTo(toEmail);
//		mailMessage.setSubject("Reset Password OTP");
//		mailMessage.setText("Hi,\n\n" + "Here is your OTP to reset your password: " + otp + "\n"
//				+ "This OTP is valid for 5 minutes.\n\n" + "If you didn't request this, please ignore this message.\n\n"
//				+ "Regards,\nAfreen Team");
//		mailSender.send(mailMessage);
//	}
//
//	public void sendOtpEmail(String toEmail, String otp) {
//		SimpleMailMessage message = new SimpleMailMessage();
//		message.setFrom(fromEmail);
//		message.setTo(toEmail);
//		message.setSubject("Account Verification OTP");
//		 message.setText("Hello User,\n\nYour OTP for account verification is: " + otp + 
//                 "\n\nThis OTP is valid for 24 hours.\n\nRegards,\nAfreen Team");
//		 
//		 mailSender.send(message);
//		
//	}
	
	 public void sendOtpEmail(String toEmail, String otp) throws MessagingException {
	        Context context = new Context();
	        context.setVariable("otp", otp);
	       context.setVariable("userName", "User"); // You can pass actual name if available
	        context.setVariable("email", toEmail);

	        String htmlContent = templateEngine.process("verifyEmail", context);

	        MimeMessage message = mailSender.createMimeMessage();
	        MimeMessageHelper helper = new MimeMessageHelper(message, true);
	        helper.setFrom(fromEmail);
	        helper.setTo(toEmail);
	        helper.setSubject("Your OTP for Email Verification");
	        helper.setText(htmlContent, true);

	        mailSender.send(message);
	    }
	 
	 public void sendResetOtpEmail(String toEmail, String otp) throws MessagingException {
		    Context context = new Context();
		    context.setVariable("otp", otp);
		    context.setVariable("userName", "User");
		    context.setVariable("email", toEmail);

		    String htmlContent = templateEngine.process("resetpassword", context); // Make sure this file exists in templates

		    MimeMessage message = mailSender.createMimeMessage();
		    MimeMessageHelper helper = new MimeMessageHelper(message, true);
		    helper.setFrom(fromEmail);
		    helper.setTo(toEmail);
		    helper.setSubject("Reset Your Password");
		    helper.setText(htmlContent, true);

		    mailSender.send(message);
		}

}
