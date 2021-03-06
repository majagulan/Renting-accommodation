import { AccommodationService } from '../../services/accommodation.service';
import { Accommodation } from '../../models/Accommodation';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, Validators } from '@angular/forms';
import { Component, OnInit } from '@angular/core';
import { fadeIn } from '../../animations';
import { ImageService } from '../../services/image.service';

@Component({
  selector: 'app-accommodation-detail',
  templateUrl: './accommodation-detail.component.html',
  styleUrls: ['./accommodation-detail.component.css'],
  animations: [fadeIn()]
})
export class AccommodationDetailComponent implements OnInit {

  private accommodationId: Number;
  private showOwnerInfo: boolean = false;
  private agentInfoBtn: boolean = false;
  private sendMessageBtn: boolean = false;
  private accommodation = {};
  private images = [];
  private firstImage = {};
  private agent = {};

  constructor(
    private accommodationService: AccommodationService,
    private imageService: ImageService,
    private formBuilder: FormBuilder,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  messageForm = this.formBuilder.group({
    content: ['', Validators.required]
  });

  ngOnInit() {
    this.accommodationId = parseInt(this.route.snapshot.params['id']);
    this.accommodationService.getAccommodation(this.accommodationId)
    .subscribe(res => {
      this.accommodation = new Accommodation(
        res.id, res.name, res.type, res.city, res.street, res.description,
        res.category, res.agent, res.images, res.imageList
      );
      this.agent = res.agent;
    }, err => {
      this.router.navigate(['**']);
    });
    this.imageService.getFirstAccommodationImage(this.accommodationId)
    .subscribe(res => {
      this.firstImage = res;
    }, err => {
      console.log(err);
    })
    this.imageService.getOtherAccommodationImages(this.accommodationId)
    .subscribe(res => {
      this.images = res;
    }, err => {
      console.log(err);
    })
  }

  showAgentInfo() {
    this.agentInfoBtn = !this.agentInfoBtn;
    document.getElementsByClassName('toggle-components')[0].setAttribute('id', 'agentInfoCollapse');
  }
}
