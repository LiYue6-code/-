package com.employment.entity;

/**
 * 招聘单位实体。
 */
public class Company {
    private Long id;
    private String name;
    private String type;
    private String contact;
    private String phone;
    private String address;
    private String description;
    private String qualificationFile;
    private String qualificationName;
    private String reviewStatus;
    private String reviewComment;
    private String creditStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getQualificationFile() {
        return qualificationFile;
    }

    public void setQualificationFile(String qualificationFile) {
        this.qualificationFile = qualificationFile;
    }

    public String getQualificationName() {
        return qualificationName;
    }

    public void setQualificationName(String qualificationName) {
        this.qualificationName = qualificationName;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    public String getCreditStatus() {
        return creditStatus;
    }

    public void setCreditStatus(String creditStatus) {
        this.creditStatus = creditStatus;
    }

    /** 是否通过资质审核，通过后方可作为岗位发布单位。 */
    public boolean isApproved() {
        return "通过".equals(reviewStatus);
    }
}
