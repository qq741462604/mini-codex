package com.minicodex.guard;


import com.minicodex.service.GitService;
import org.springframework.stereotype.Component;



import java.util.Date;



@Component
public class ExecutionGuard {



    private final GitService gitService;



    public ExecutionGuard(
            GitService gitService
    ){

        this.gitService = gitService;

    }




    public ChangeSnapshot beforeExecute(
            String task
    ){


        String commitId =
                gitService.commit(
                        "before agent change"
                );


        return ChangeSnapshot.builder()
                .task(task)
                .commitId(commitId)
                .time(new Date())
                .build();


    }





    public boolean rollback(
            ChangeSnapshot snapshot
    ){


        return gitService.reset(
                snapshot.getCommitId()
        );


    }


}