import { Component, OnInit } from '@angular/core';
import { fadeIn } from '../../animations';
import {ApartmentService } from '../../services/apartment.service';
import { FormBuilder, Validators, FormGroup, FormControl } from '@angular/forms';
import {DomSanitizer} from '@angular/platform-browser';
import { SecurityContext } from "@angular/core";
import {AccommodationService } from '../../services/accommodation.service';
import { Router, ActivatedRoute, Params } from '@angular/router';

@Component({
  selector: 'app-apartment-table',
  templateUrl: './apartment-table.component.html',
  styleUrls: ['./apartment-table.component.css'],
  animations: [fadeIn()]
})
export class ApartmentTableComponent implements OnInit {
    images = [];
    imag;
    accommodationId: any;
    accommodation: any;
    temp: any;
    
  constructor(private apartmentService : ApartmentService, 
          private accommodationService : AccommodationService, 
          private formBuilder: FormBuilder, 
          private router: Router, 
          private dom: DomSanitizer,
          private activatedRoute: ActivatedRoute) {
//      <img [src]="_DomSanitizationService.bypassSecurityTrustUrl(imgSrcProperty)"/>
      
      this.activatedRoute.params.subscribe((params: Params) => {
          this.accommodationId = params['id'];
        });
      
      this.accommodationService.getAccommodation(this.accommodationId).subscribe(res => 
      {
          this.temp = res['return'];
          this.accommodation = {
              'id': this.temp.id,
              'name': this.temp.name,
              'type': this.temp.type,
              'city': this.temp.city,
              'street': this.temp.street,
              'country': this.temp.country,
              'description': this.temp.description,
              'category': this.temp.category,
              'image': this.temp.image 
          }
          let imgs = this.accommodation.image.split(';');
          
          for(let i = 0; i < imgs.length - 1; i++)
          {
              console.log(imgs[i]);
              let pic = imgs[i].split("imgs")[1];
              console.log(pic);
//              this.imag = dom.bypassSecurityTrustUrl("../assets/imgs/" + pic);
             this.images.push(dom.bypassSecurityTrustUrl("../assets/imgs/" + pic)); 
             
          }
          console.log(this.accommodation.image);
      },
      err => {this.router.navigate(['/notFound']);
      });
      
//      this.images = dom.bypassSecurityTrustUrl("../assets/out29.png");
      /*    [
                     {"url":"G://tanja//slike//out-1.png"},
                     {"url":"G://tanja//slike//out9.png"}
                    
                       ];*/
//      var imageFilePath = $"{ImageFolder}{imgId}.jpg";
//      var imageFileStream = System.IO.File.OpenRead(imageFilePath);
//      return File(imageFileStream, "image/jpeg");
   
      
      
      
  }

  ngOnInit() {
  }
  
  selectedImage;
  
  setSelectedImage(image){
     this.selectedImage= image;    
  }

}
