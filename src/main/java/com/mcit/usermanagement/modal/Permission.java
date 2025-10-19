package com.mcit.usermanagement.modal;

import com.mcit.usermanagement.util.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "permissions")
public class Permission extends BaseEntity {

    private String permissionName;

}
