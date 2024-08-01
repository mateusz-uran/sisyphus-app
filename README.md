<p align="center">
The application is still in development, so it currently has only a few features.
</p>

----

<a name="readme-top"></a>
<!-- PROJECT LOGO -->
<br />
<div align="center">

<h3 align="center">Sisyphus App</h3>

  <p align="center">
    Manage the thousands of applications you send while searching for your first job as a programmer.
    <br />
    <br />
  </p>
</div>

<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a>
      <ul>
        <li><a href="#api">API</a></li>
        <li><a href="#frontend">Frontend</a></li>
      </ul>
    </li>
    <li><a href="#contact">Contact</a></li>
    <li><a href="#acknowledgments">Acknowledgments</a></li>
  </ol>
</details>

<!-- ABOUT THE PROJECT -->

## About The Project

Are you looking for your first job as a programmer and sending out an average of a dozen resumes per day? When applying for a specific position, 
we usually try to tailor the resume to the job offer, resulting in several versions of the same document. 
The application helps manage this by grouping all job offers to which you have sent a particular resume according to the PDF file.

Features:

* Uploading PDF file via handy drag and drop window
* Add multiple or single link to a specific group
* Change the status of each application from "sent" to "hired"
* Hired animation
* Scraper for applied job offers

### Built With

Java 17, Python, Spring Boot 3.3, Angular 18, Angular Material and Animations

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- GETTING STARTED -->

## Getting Started

### Installation

* to run backend, frontend and mongodb as one container run in root direcotry:
  ```
  docker compose up -d
  ```
  
Now applicaiton will be available:
* backend  - http://localhost:8088/api/v1
* frontend - http://localhost:9090/ or http://localhost:4200 (if running from compiler)
* python - http://127.0.0.1:5858/scrape


<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- USAGE EXAMPLES -->

## Usage

// how to use application 

### API

#### 1. WorkGroup controller

### GET, POST, DELETE

| Method    | Endpoint                                                   | Description                            |
|-----------|------------------------------------------------------------|----------------------------------------|
| GET       | `/api/v1/group/all`                                        | [get all groups](#get-all-groups)      |
| GET       | `/api/v1/group/single/{workGroupId}`                       | [get single group](#get-single-group)  |
| POST      | `/api/v1/group/create?string`                              | [upload pdf](#upload-pdf)              |
| DELETE    | `/api/v1/group/delete/{workGroupId}`                       | [delete group](#delete-group)          |

---

#### 2. WorkApplicaitons controller

### GET, PATCH, POST, DELETE

| Method    | Endpoint                                                   | Description                                                         |
|-----------|------------------------------------------------------------|---------------------------------------------------------------------|
| GET       | `/api/v1/applications/all/{workGroupId}`                   | [get all applications from group](#get-all-applications-from-group) |
| PATCH     | `/api/v1/applications/single/{applicationId}`              | [update single application](#update-single-application)             |
| POST      | `/api/v1/applications/save/{workGroupId}`                  | [save applications](#save-applications)                             |
| DELETE    | `/api/v1/applications/delete/{applicationId}`              | [delete application](#delete-application)                           |
| POST      | `/api/v1/applications/spec/{applicationId}`                | [scrape application](#scrape-application)                           |

---

#### Get All Groups

endpoint: `/api/v1/group/all`

|  Params  | Required |  Type  | Description                               |
|:--------:|:--------:|:------:|-------------------------------------------|
|     -    |   false  |      - |                     -                     |

_Response example_ </br>

```
[
  {
    "id": "string",
    "cvData": "string",
    "cvFileName": "string",
    "creationTime": "string",
    "applied": 0,
    "denied": 0,
    "inProgress": 0,
    "isHired": true
  }
]
```

---

#### Get single group

endpoint: `/api/v1/group/single/1`

|  Path variable  | Required |  Type  | Description                                          |
|:---------------:|:--------:|:------:|------------------------------------------------------|
|   workGroupId   |  true    | string | Group id to identify which object to pull from the database |

_Response example_ </br>
```
{
  "id": "string",
  "cvData": "string",
  "cvFileName": "string",
  "creationTime": "string",
  "applied": 0,
  "denied": 0,
  "inProgress": 0,
  "isHired": true
}
```

---

#### Upload pdf

endpoint: `/api/v1/group/create`

|  Params  | Required |  Type  | Description                               |
|:--------:|:--------:|:------:|-------------------------------------------|
|    cv    |   true   | string | PDF file as string                        |

_Response example_ </br>
```
// status
HttpStatus.OK 200
```

---

#### Delete group

endpoint: `/api/v1/group/delete/1`

|  Path variable  | Required |  Type  | Description                                            |
|:---------------:|:--------:|:------:|--------------------------------------------------------|
|   workGroupId   |  true    | string | Group id to identify which object to delete from the database. |

_Response example_ </br>
```
// status
HttpStatus.OK 200
```

---

#### Get all applications from group

endpoint: `/api/v1/applications/all/1`

|  Path variable  | Required |  Type  | Description                                             |
|:---------------:|:--------:|:------:|---------------------------------------------------------|
|   workGroupId   |  true    | string | Group id to identify which applications to pull from the group |

_Response example_ </br>
```
[
  {
    "id": "string",
    "workUrl": "string",
    "appliedDate": "string",
    "status": "IN_PROGRESS"
  }
]
```

---

#### Update single application

endpoint: `/api/v1/applications/update/1/hired`

|  Path variable   | Required |  Type  | Description                                                |
|:----------------:|:--------:|:------:|------------------------------------------------------------|
|  applicationId   |  true    | string | Application id to identify which object to pull from database |
|     status       |  true    | string | New application status                                     |

_Response example_ </br>
```
{
  "id": "string",
  "workUrl": "string",
  "appliedDate": "string",
  "status": "IN_PROGRESS"
}
```

---

#### Save applications

endpoint: `/api/v1/applications/save/1`

|  Path variable  | Required |  Type  | Description                                             |
|:---------------:|:--------:|:------:|---------------------------------------------------------|
|   workGroupId   |  true    | string | Group id to identify where to save applications            |

_Request example_ </br>
```
[
  {
    "workUrl": "string"
  }
]
```

_Response example_ </br>
```
// status
HttpStatus.OK 200
```

---

#### Delete application

endpoint: `/api/v1/applications/save/1`

|  Path variable   | Required |  Type  | Description                                                  |
|:----------------:|:--------:|:------:|--------------------------------------------------------------|
|  applicationId   |  true    | string | Application id to identify which object to delete from the database |

_Response example_ </br>
```
// status
HttpStatus.OK 200
```

---

#### Scrape application

endpoint: `/api/v1/applications/spec/1`

|  Path variable   | Required |  Type  | Description                                                  |
|:----------------:|:--------:|:------:|--------------------------------------------------------------|
|  applicationId   |  true    | string | Application id to identify if the specification already exists   |

_Response example_ </br>
```
{
    "company_name": "Software Mansion",
    "requirements_expected": [],
    "technologies_expected": [
        "JavaScript",
        "TypeScript",
        "React",
        "React native"
    ]
}
```

---

### Frontend
- only polish language available - for now

1. The first page currently displays all the added groups (each group corresponds to an added PDF file). Each group has counters related to the applications within it. The user can open the file in the browser, delete the group, or browse all the previously added applications for that group.

<p align="center">
  <img src="https://github.com/mateusz-uran/sisyphus-app/blob/readme/readmeimg/home.png">
</p>

</br>

2. The PDF upload window allows you to add a file in two ways: by dragging the file into the area or by clicking a button that opens a file browsing window. The application includes a progress bar during the file upload process, so users can monitor the progress.

<p align="center">
  <img src="https://github.com/mateusz-uran/sisyphus-app/blob/readme/readmeimg/upload.png">
</p>

</br>

3. While browsing a group, you can see all the applications made with a given resume. Currently, only the link is visible, but a preview will be available later. Additionally, you can change the status of each application. The default initial status is SENT. Users can update the status during the recruitment process, and there is also a HIRED status, which triggers an animation.

<p align="center">
  <img src="https://github.com/mateusz-uran/sisyphus-app/blob/readme/readmeimg/applications.png">
</p>

3.1. New feature: The URL of added job offers is passed to a new Python API, which scrapes the information. The response includes the technologies required for the specific job offer. Additionally, an expandable panel displays a list of requirements, if this information is available and can be fetched from the job offer link. This will be shown on the interface.

   <p align="center">
    <img src="https://github.com/mateusz-uran/sisyphus-app/blob/readme/readmeimg/specifications.png">
   </p>

</br>

4. Above the list of applications, there is a form for adding links to job offers that the user has applied to. Multiple links can be added at once, which speeds up the entire process.

<p align="center">
  <img src="https://github.com/mateusz-uran/sisyphus-app/blob/readme/readmeimg/add links.png">
</p>

</br>

5. When the user finds a job, they can change the status of the application to HIRED. This will trigger a confetti animation on the screen and display a congratulatory message.

<p align="center">
  <img src="https://github.com/mateusz-uran/sisyphus-app/blob/readme/readmeimg/hired.png">
</p>

</br>

6. Additionally, a trophy animation will appear next to the group where the application status has been changed to HIRED, making it easier to identify the successful application later on.

<p align="center">
  <img src="https://github.com/mateusz-uran/sisyphus-app/blob/readme/readmeimg/hired2.png">
</p>

---


<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- CONTACT -->

## Contact

Email - mateusz.uranowski@onet.pl

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- ACKNOWLEDGMENTS -->

## Acknowledgments

Resources I've used to create this project!

* to create CI/CD pipeline - ali-bouali https://github.com/ali-bouali/book-social-network

<p align="right">(<a href="#readme-top">back to top</a>)</p>

