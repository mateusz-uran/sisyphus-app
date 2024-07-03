import { Routes } from '@angular/router';
import { AppComponent } from './app.component';
import { GroupSpecComponent } from './group-spec/group-spec.component';
import { HomeComponent } from './home/home.component';

export const routes: Routes = [
  {
    path: '',
    component: HomeComponent,
    title: 'Strona główna',
  },
  {
    path: 'group/:id',
    component: GroupSpecComponent,
    title: 'Grupa',
  },
];
