package com.keep.changes.email;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.keep.changes.exception.ApiException;
import com.keep.changes.user.token.TokenService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

	@Value("${spring.mail.username}")
	private String FROM_EMAIL;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private SpringTemplateEngine templateEngine;

	@Autowired
	private TokenService tokenService;

	@Async
	@Override
	public void sendEmail(String to, String username, EmailTemplateName emailTemplate, String subject) {

		String templateName;
		if (emailTemplate == null) {
			templateName = "confirm-email";
		} else {
			templateName = emailTemplate.getName();
		}

		String activationCode = this.tokenService.generateAndSaveActivationToken(to);

		MimeMessage mimeMessage = mailSender.createMimeMessage();
		MimeMessageHelper messageHelper;
		try {
			messageHelper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED,
					StandardCharsets.UTF_8.name());

			Map<String, Object> properties = new HashMap<>();
			properties.put("username", username);
			properties.put("activation_code", activationCode);

			Context context = new Context();
			context.setVariables(properties);

			messageHelper.setFrom(FROM_EMAIL);
			messageHelper.setTo(to);
			messageHelper.setSubject(subject);

			String template = this.templateEngine.process(templateName, context);
			messageHelper.setText(template, true);

			this.mailSender.send(mimeMessage);

		} catch (MessagingException e) {
			throw new ApiException("Could not sent otp to mail. Try again.", HttpStatus.BAD_REQUEST, false);
		}

	}
}
