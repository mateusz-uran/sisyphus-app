import json
import requests
import tldextract
from bs4 import BeautifulSoup

USER_AGENT = 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) ' \
             'AppleWebKit/537.36 (KHTML, like Gecko) ' \
             'Chrome/91.0.4472.124 Safari/537.36'


def fetch_html(url, headers):
    response = requests.get(url, headers=headers)
    response.raise_for_status()
    response.encoding = 'utf-8'
    return response.text


def extract_json_data(html, script_id):
    soup = BeautifulSoup(html, 'html.parser')
    script_tag = soup.find('script', id=script_id)
    if not script_tag:
        raise ValueError(f"Script tag with id '{script_id}' not found")
    return json.loads(script_tag.string)


def extract_description(description_html):
    description_soup = BeautifulSoup(description_html, 'html.parser')
    return [item.get_text(strip=True)
            for item in
            description_soup.find_all('li')]


def extract_domain(url):
    return tldextract.extract(url).domain
