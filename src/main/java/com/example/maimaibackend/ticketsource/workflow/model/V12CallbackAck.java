package com.example.maimaibackend.ticketsource.workflow.model;

public record V12CallbackAck(String eventId, boolean accepted, boolean duplicate, String processStatus) {}
