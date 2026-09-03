package com.example.maimaibackend.ticketsource.workflow.model;

import java.util.List;

public record V12BatchResult(int requested, int succeeded, int failed, List<Long> succeededIds, List<String> failures) {}
