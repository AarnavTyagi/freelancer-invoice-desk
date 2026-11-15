package com.freelancedesk.service;

import com.freelancedesk.dao.BillableServiceDao;
import com.freelancedesk.dao.ClientDao;
import com.freelancedesk.dao.FreelancerDao;
import com.freelancedesk.exception.RecordNotFoundException;
import com.freelancedesk.exception.ValidationException;
import com.freelancedesk.model.BillableService;
import com.freelancedesk.model.Client;
import com.freelancedesk.model.Freelancer;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Manages freelancer profiles, client records and billable service descriptions. */
public class ProfileService {
    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final FreelancerDao freelancerDao;
    private final ClientDao clientDao;
    private final BillableServiceDao serviceDao;

    public ProfileService(FreelancerDao freelancerDao, ClientDao clientDao, BillableServiceDao serviceDao) {
        this.freelancerDao = freelancerDao;
        this.clientDao = clientDao;
        this.serviceDao = serviceDao;
    }

    public Freelancer addFreelancer(String name, String email, String profession)
            throws ValidationException, SQLException {
        requireText(name, "Name");
        requireText(profession, "Profession");
        requireEmail(email);
        try {
            return freelancerDao.save(new Freelancer(0, name, email, profession));
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new ValidationException("A freelancer with email " + email + " already exists.");
        }
    }

    public Client addClient(String name, String email, String company)
            throws ValidationException, SQLException {
        requireText(name, "Name");
        requireText(company, "Company");
        requireEmail(email);
        try {
            return clientDao.save(new Client(0, name, email, company));
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new ValidationException("A client with email " + email + " already exists.");
        }
    }

    public BillableService addService(int freelancerId, String description, BigDecimal unitRate,
                                      BigDecimal taxPercent)
            throws ValidationException, RecordNotFoundException, SQLException {
        freelancerDao.findById(freelancerId)
                .orElseThrow(() -> new RecordNotFoundException("Freelancer " + freelancerId + " not found."));
        requireText(description, "Description");
        if (unitRate.signum() <= 0) {
            throw new ValidationException("Unit rate must be greater than zero.");
        }
        if (taxPercent.signum() < 0 || taxPercent.compareTo(HUNDRED) > 0) {
            throw new ValidationException("Tax percent must be between 0 and 100.");
        }
        return serviceDao.save(new BillableService(0, freelancerId, description, unitRate, taxPercent));
    }

    public List<Freelancer> getFreelancers() throws SQLException {
        return freelancerDao.findAll();
    }

    public List<Client> getClients() throws SQLException {
        return clientDao.findAll();
    }

    public List<BillableService> getServicesOf(int freelancerId) throws SQLException {
        return serviceDao.findByFreelancer(freelancerId);
    }

    public Map<Integer, Client> clientsById() throws SQLException {
        return clientDao.findAll().stream().collect(Collectors.toMap(Client::getId, Function.identity()));
    }

    private void requireText(String value, String field) throws ValidationException {
        if (value == null || value.isBlank()) {
            throw new ValidationException(field + " cannot be empty.");
        }
    }

    private void requireEmail(String email) throws ValidationException {
        if (email == null || !EMAIL.matcher(email).matches()) {
            throw new ValidationException("Enter a valid email address.");
        }
    }
}
