Popular Movies (Stage 2)
======

This is Popular Movies Stage 2 of Udacity's Android Developer Nanodegree.
The purpose of this project was to build an app that helps users discover popular and top rated movies on the web.
It fetches themoviedb.org API to display the movies data, that way the content provided is always up-to-date and relevant.

For Popular Movies Stage 1 project click [HERE](https://github.com/henriquenfaria/popular-movies-stage-1)



Stage 2 Features
-----

This is the last and final part of Popular Movies Project.
It contains the following features:

- Upon launch, it presents users with an grid arrangement of movie posters and titles;
- Selecting a movie from the list displays more infomation about it, such as: original title, plot synopsis, user rating, release date, trailers and user reviews;
- Users can mark movies as favorites. All favorite movies are stored in the application private database for offline view.
- Users can share movies trailers;
- Configurable movies sort order via settings (popular, top rated and favorites);
- Optimized for both phones and tablets.


Screens
------

![alt text](https://github.com/henriquenfaria/popular-movies-stage-2/blob/master/art/stage2_phone_portrait_list.png "Phone Details")
![alt text](https://github.com/henriquenfaria/popular-movies-stage-2/blob/master/art/stage2_phone_portrait_detail.png "Phone Details")
![alt text](https://github.com/henriquenfaria/popular-movies-stage-2/blob/master/art/stage2_tablet_landscape.png "Tablet")




Instructions
------

You need to create a free account on themoviedb.org and generate your personal API key. More info [HERE](https://www.themoviedb.org/documentation/api).

In your gradle.properties file, put your generated API Key like this: `MyTheMovieDbMapApiKey="my_generated_apy_key_value"`




Libraries
------

This project uses the following libraries:

[Glide](https://github.com/bumptech/glide)




License
------

> Copyright 2016 Henrique Nunes Faria

> Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

> http://www.apache.org/licenses/LICENSE-2.0

> Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
