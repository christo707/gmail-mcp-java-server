package com.christo.agentic.ai;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Builder
@Data
public class Email {
    private String from;
    private String to;
    private String subject;
    private String body;
    private Date date;
}
