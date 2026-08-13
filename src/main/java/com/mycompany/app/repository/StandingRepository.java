package com.mycompany.app.repository;

import com.mycompany.app.model.Standing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface StandingRepository extends JpaRepository<Standing,Long>{
    
}
