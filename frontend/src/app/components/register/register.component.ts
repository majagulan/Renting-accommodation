import { Component, OnInit } from '@angular/core';
import { fadeIn } from '../../animations';
import { FormBuilder, Validators } from '@angular/forms';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css'],
  animations: [fadeIn()]
})
export class RegisterComponent implements OnInit {

  constructor(private userService: UserService, private formBuilder: FormBuilder) { }

  registerForm = this.formBuilder.group({
    email: ['', Validators.compose([
      Validators.required,
      Validators.pattern('^[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,4}$')
    ])],
    name: ['', Validators.required],
    surname: ['', Validators.required],
    street: ['', Validators.required],
    city: ['', Validators.required],
    password1: ['', Validators.compose([
      Validators.minLength(8),
      Validators.required
    ])],
    password2: ['', Validators.compose([
      Validators.minLength(8),
      Validators.required
    ])]
  });

  ngOnInit() {
  }

  register() {
    this.userService.registerUser(this.registerForm.value)
    .subscribe(
      res => console.log(res),
      err => console.log(err)
    );
  }
}