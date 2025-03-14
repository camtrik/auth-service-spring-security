package com.ebbilogue.authservice.models;

import jakarta.persistence.*;

// 将Java类标记为一个实体类，表示这个类对应数据库中的一个表(roles)
@Entity
@Table(name = "roles")
public class Role {
    @Id 
    // 指定主键生成策略为自增长
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; 

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ERole name; 

    public Role() {

    }

    public Role(ERole name) {
        this.name = name;
    }

    public Integer getId() {
        return id; 
    }

    public void setId(Integer id) {
        this.id = id; 
    }

    public ERole getName() {
        return name; 
    }

    public void setName(ERole name) {
        this.name = name; 
    }
        
}
