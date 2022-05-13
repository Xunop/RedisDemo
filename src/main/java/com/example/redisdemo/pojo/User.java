package com.example.redisdemo.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @Author xun
 * @create 2022/5/11 21:38
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class User implements Serializable {
    private String name;
    private Integer age;
}
