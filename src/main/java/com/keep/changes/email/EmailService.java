package com.keep.changes.email;

import jakarta.mail.MessagingException;

public interface EmailService {

	void sendEmail(String to, String username, EmailTemplateName emailTemplate, String subject);
}
